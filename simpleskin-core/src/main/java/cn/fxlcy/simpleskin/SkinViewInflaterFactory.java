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

final class SkinViewInflaterFactory implements BaseViewInflater.Factory {

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


    @Override
    public View onCreateView(View parent, View view, String name, Context context, AttributeSet attributeSet) {
        final TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.SkinStyle);

        final boolean defaultUse = a.getBoolean(R.styleable.SkinStyle_skin, mConfig.mDefaultUse);

        if (view == null || !defaultUse) {
            a.recycle();
            return view;
        }

        //是否是自定义更改皮肤的逻辑
        final boolean customChanger = view instanceof SkinViewChanger && ((SkinViewChanger) view).skinEnabled();

        final List<SkinApplicator<? extends View>> skinApplicatorList;

        if (customChanger) {
            skinApplicatorList = null;
        } else {
            skinApplicatorList =
                    SkinManager.getInstance().getSkinApplicators(view.getClass(), this);

            if (skinApplicatorList.size() == 0) {
                a.recycle();
                return view;
            }
        }

        final String[] tmp = splitAttrString(a.getString(R.styleable.SkinStyle_skinWhiteAttr));
        List<String> whiteArr = null;
        if (tmp != null) {
            whiteArr = new LinkedList<>(Arrays.asList(tmp));
        }

        final String[] blackAttr = splitAttrString(a.getString(R.styleable.SkinStyle_skinBlackAttr));

        a.recycle();

        final int count = attributeSet.getAttributeCount();
        List<SkinViewAttr> skinViewAttrs;
        final boolean hasSkin = SkinManager.getInstance().getCurrentSkin() != null;

        int[] whiteAttrIds = null;

        if (whiteArr != null && whiteArr.size() > 0) {
            final int size = whiteArr.size();
            SparseArray<String> whiteArrMap = null;

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
            }
        }


        //合并两个数组并去重
        whiteAttrIds = mergeIntArray(mConfig.getWhiteAttrs(), whiteAttrIds);

        skinViewAttrs = resolveWhiteAttrs(context, attributeSet, view, whiteAttrIds, customChanger, hasSkin, skinApplicatorList);

        final int[] blackAttrIds = mConfig.getBlackAttrs();

        for (int i = 0; i < count; i++) {
            int intValue = attributeSet.getAttributeResourceValue(i, 0);

            if (intValue != 0) {
                final int attrId = attributeSet.getAttributeNameResource(i);

                //如果属性是在黑名单或白名单属性中continue
                if (Objects.contain(blackAttrIds, attrId) || Objects.contain(whiteAttrIds, attrId)) {
                    continue;
                }

                final Boolean isAndroidAttr = isAndroidAttr(context, attributeSet, attrId, i);

                if (isAndroidAttr == null) {
                    continue;
                }

                String attrName = attributeSet.getAttributeName(i);

                if (!isAndroidAttr) {
                    attrName = CUSTOM_ATTR_PREFIX + attrName;
                }

                //如果属性是在黑名单属性中continue
                if (Objects.contain(blackAttr, attrName)) {
                    continue;
                }

                if (skinViewAttrs == null) {
                    skinViewAttrs = new LinkedList<>();
                }

                if (customChanger) {
                    SkinViewAttr attr = new SkinViewAttr(attrId, intValue, null);
                    skinViewAttrs.add(attr);
                } else {
                    SkinApplicator skinApplicator = getSkinApplicator(skinApplicatorList, attrId);

                    if (skinApplicator != null) {
                        SkinViewAttr attr = new SkinViewAttr(attrId, intValue, skinApplicator);

                        skinViewAttrs.add(attr);

                        //如果当前有皮肤直接应用皮肤
                        if (hasSkin) {
                            attr.apply(view);
                        }
                    }
                }
            }
        }


        if (skinViewAttrs != null) {
            SkinViewManager.getInstance().addSkinView(view, skinViewAttrs);


            //触发自定义换肤逻辑
            if (customChanger && hasSkin) {
                ((SkinViewChanger) view).onSkinChanged(new SkinViewChanger.Helper(view, skinViewAttrs),
                        SkinManager.getInstance().getResources(context), context.getTheme());
            }
        }


        return view;
    }


    private static int[] mergeIntArray(int[] arr1, int[] arr2) {
        if (arr1 == null || arr1.length == 0) {
            return arr1;
        } else if (arr2 == null || arr2.length == 0) {
            Arrays.sort(arr2);
            return arr1;
        } else {
            int arr1Len = arr1.length;
            int arr2Len = arr2.length;
            int[] newArr = Arrays.copyOf(arr1, arr1Len + arr2Len);

            int off = 0;

            for (int value : arr2) {
                if (!Objects.contain(arr1, value)) {
                    newArr[arr1Len + off] = value;
                    off++;
                }
            }

            if (off == 0) {
                newArr = arr1;
            } else if (off != arr2Len) {
                newArr = Arrays.copyOf(newArr, arr1Len + off);
            }

            if (off != 0) {
                Arrays.sort(newArr);
            }

            return newArr;
        }
    }

    private static String[] splitAttrString(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        } else {
            return str.split(",");
        }
    }


    //解析白名单属性
    private static List<SkinViewAttr> resolveWhiteAttrs(Context context, AttributeSet attributeSet, View view, int[] whiteAttrIds, boolean customChanger
            , boolean hasSkin, List<SkinApplicator<? extends View>> skinApplicatorList) {
        final TypedArray ta = context.obtainStyledAttributes(attributeSet, whiteAttrIds);

        int whiteIndex = whiteAttrIds.length;

        List<SkinViewAttr> skinViewAttrs = null;

        for (int i = 0; i < whiteIndex; i++) {
            //获取白名单中所有attr 的value的resourceid
            int id = ta.getResourceId(i, 0);

            if (id != 0) {
                final int attrId = whiteAttrIds[i];
//                final String attrName = whiteArrMap.get(attrId);

                if (skinViewAttrs == null) {
                    skinViewAttrs = new LinkedList<>();
                }

                if (customChanger) {
                    skinViewAttrs.add(new SkinViewAttr(attrId, id, null));
                } else {
                    final SkinApplicator skinApplicator = getSkinApplicator(skinApplicatorList, attrId);

                    if (skinApplicator != null) {
                        SkinViewAttr attr = new SkinViewAttr(attrId, id, skinApplicator);


                        skinViewAttrs.add(attr);

                        //如果当前有皮肤直接应用皮肤
                        if (hasSkin) {
                            attr.apply(view);
                        }
                    }
                }
            }
        }

        ta.recycle();

        return skinViewAttrs;
    }



    private static SkinApplicator<? extends View> getSkinApplicator(List<SkinApplicator<? extends View>> skinApplicatorList, int attrId) {
        for (SkinApplicator<? extends View> skinApplicator : skinApplicatorList) {
            int[] attrs = skinApplicator.getAttrIds();

            for (int a : attrs) {
                if (attrId == a) {
                    return skinApplicator;
                }
            }
        }

        return null;
    }



    //判断是否是android系统自带的属性 如果返回null代表是style 之类的无namespace的属性
    private static Boolean isAndroidAttr(Context context, AttributeSet attributeSet, int attrId, int index) {

        //过滤掉skinWhiteAttr和skinBlackAttr
        if (attrId == 0 || android.R.attr.id == attrId || R.attr.skinWhiteAttr == attrId ||
                R.attr.skinBlackAttr == attrId || R.attr.skin == attrId) {
            return null;
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            final String name = attributeSet.getAttributeNamespace(index);

            if (ANDROID_NAMESPACE.equals(name)) {
                return true;
            } else if (APP_NAMESPACE.equals(name)) {
                return false;
            } else {
                return null;
            }
        } else {
            final String packageName = context.getResources().getResourcePackageName(attrId);

            return SYSTEM_PACKAGE.equals(packageName);
        }
    }
}
