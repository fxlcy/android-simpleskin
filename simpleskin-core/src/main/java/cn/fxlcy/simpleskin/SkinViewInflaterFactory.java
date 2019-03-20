package cn.fxlcy.simpleskin;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.huazhen.library.simplelayout.inflater.BaseViewInflater;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cn.fxlcy.lib.simpleskin.R;
import cn.fxlcy.simpleskin.util.Objects;

public class SkinViewInflaterFactory implements BaseViewInflater.Factory {

    private final static String TAG = "SkinViewInflaterFactory";

    private final static String APP_NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private final static String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

    private final static String SYSTEM_PACKAGE = "android";

    //自定义属性的前缀
    private final static String CUSTOM_ATTR_PREFIX = "app:";


    private static SkinViewInflaterFactory sDefault;


    final SkinManager.SkinConfig mConfig;


    private SkinViewInflaterFactory() {
        mConfig = SkinManager.SkinConfig.EMPTY;
    }


    public SkinViewInflaterFactory(SkinManager.SkinConfig config) {
        mConfig = config;
    }


    public static SkinViewInflaterFactory getDefault() {
        if (sDefault == null) {
            synchronized (SkinViewInflaterFactory.class) {
                if (sDefault == null) {
                    sDefault = new SkinViewInflaterFactory();
                }
            }
        }

        return sDefault;
    }


    private SkinApplicator<? extends View> getSkinApplicator(List<SkinApplicator<? extends View>> skinApplicatorList, String attrName) {
        for (SkinApplicator<? extends View> skinApplicator : skinApplicatorList) {
            String[] attrs = skinApplicator.attrs();
            for (String a : attrs) {
                if (attrName.equals(a)) {
                    return skinApplicator;
                }
            }
        }

        return null;
    }


    private static String[] splitAttrString(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        } else {
            return str.split(",");
        }
    }

    //判断是否是android系统自带的属性
    private static boolean isAndroidAttr(Context context, AttributeSet attributeSet, int index) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            String name = attributeSet.getAttributeNamespace(index);
            return ANDROID_NAMESPACE.equals(name);
        } else {
            int id = attributeSet.getAttributeNameResource(index);
            String packageName = context.getResources().getResourcePackageName(id);
            return SYSTEM_PACKAGE.equals(packageName);
        }
    }

    @Override
    public View onCreateView(View parent, View view, String name, Context context, AttributeSet attributeSet) {

        final TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.SkinStyle);

        final boolean defaultUse = a.getBoolean(R.styleable.SkinStyle_skin, mConfig.defaultUse);

        if (view == null || !defaultUse) {
            a.recycle();
            return view;
        }


        if (view instanceof SkinViewChanger && ((SkinViewChanger) view).skinEnabled()) {
            a.recycle();

            SkinViewManager.getInstance().addSkinView(view, null);

            return view;
        }

        final List<SkinApplicator<? extends View>> skinApplicatorList =
                SkinManager.getInstance().getSkinApplicators(view.getClass(), this);

        if (skinApplicatorList.size() == 0) {
            a.recycle();
            return view;
        }


        final String[] blackAttr = splitAttrString(a.getString(R.styleable.SkinStyle_skinBlackAttr));
        final String[] tmp = splitAttrString(a.getString(R.styleable.SkinStyle_skinWhiteAttr));
        List<String> whiteArr = null;
        if (tmp != null) {
            whiteArr = new LinkedList<>(Arrays.asList(tmp));
        }


        a.recycle();

        final int count = attributeSet.getAttributeCount();
        LinkedList<SkinViewAttr> skinViewAttrs = null;
        final boolean hasSkin = SkinManager.getInstance().getCurrentSkin() != null;

        for (int i = 0; i < count; i++) {
            int intValue = attributeSet.getAttributeResourceValue(i, -1);

            if (intValue != -1) {
                String attrName = attributeSet.getAttributeName(i);
                if (!isAndroidAttr(context, attributeSet, i)) {
                    attrName = CUSTOM_ATTR_PREFIX + attrName;
                }

                //如果属性是在黑名单属性中break
                if (Objects.contain(blackAttr, attrName)) {
                    break;
                }

                if (whiteArr != null) {
                    whiteArr.remove(attrName);
                }

                SkinApplicator skinApplicator = getSkinApplicator(skinApplicatorList, attrName);

                if (skinApplicator != null) {
                    SkinViewAttr attr = new SkinViewAttr();
                    attr.attrName = attrName;
                    attr.value = intValue;
                    attr.applicator = skinApplicator;


                    if (skinViewAttrs == null) {
                        skinViewAttrs = new LinkedList<>();
                    }
                    skinViewAttrs.add(attr);


                    //如果当前有皮肤直接应用皮肤
                    if (hasSkin) {
                        attr.apply(view);
                    }
                }
            }
        }


        if (whiteArr != null && whiteArr.size() > 0) {
            final int size = whiteArr.size();
            SparseArray<String> whiteArrMap = null;
            int[] whiteAttrIds = null;

            int whiteIndex = 0;


            //获取白名单中的attr的所有的属性id
            for (String attr : whiteArr) {
                final int attrId;
                //自定义属性包含'app:'前缀
                if (attr.startsWith(CUSTOM_ATTR_PREFIX)) {
                    final String newAttr = attr.substring(CUSTOM_ATTR_PREFIX.length());
                    attrId = context.getResources().getIdentifier(newAttr, "attr", context.getPackageName());
                } else {
                    attrId = context.getResources().getIdentifier(attr, "attr", SYSTEM_PACKAGE);
                }

                if (attrId != 0) {
                    if (whiteArrMap == null) {
                        whiteArrMap = new SparseArray<>(size);
                    }

                    whiteArrMap.put(attrId, attr);

                    if (whiteAttrIds == null) {
                        whiteAttrIds = new int[size];
                    }

                    whiteAttrIds[whiteIndex] = attrId;

                    whiteIndex++;
                }

            }

            if (whiteAttrIds != null) {
                if (whiteIndex != size) {
                    whiteAttrIds = Arrays.copyOf(whiteAttrIds, whiteIndex);
                }

                //attr从小到大排序(必须)
                Arrays.sort(whiteAttrIds);

                final TypedArray ta = context.obtainStyledAttributes(attributeSet, whiteAttrIds);

                for (int i = 0; i < whiteIndex; i++) {
                    //获取白名单中所有attr 的value的resourceid
                    int id = ta.getResourceId(i, -1);

                    if (id != -1) {
                        final String attrName = whiteArrMap.get(whiteAttrIds[i]);
                        SkinApplicator skinApplicator = getSkinApplicator(skinApplicatorList,
                                attrName);

                        if (skinApplicator != null) {
                            SkinViewAttr attr = new SkinViewAttr();
                            attr.attrName = attrName;
                            attr.value = id;
                            attr.applicator = skinApplicator;


                            if (skinViewAttrs == null) {
                                skinViewAttrs = new LinkedList<>();
                            }
                            skinViewAttrs.add(attr);


                            //如果当前有皮肤直接应用皮肤
                            if (hasSkin) {
                                attr.apply(view);
                            }
                        }
                    }
                }

                ta.recycle();
            }

        }


        if (skinViewAttrs != null) {
            SkinViewManager.getInstance().addSkinView(view, skinViewAttrs);
        }


        return view;
    }
}
