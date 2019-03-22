package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import cn.fxlcy.simpleskin.util.ContextUtils;


/**
 * 所有换肤的view集合管理类
 */
public final class SkinViewManager {


    private final WeakHashMap<Context, SkinViewWeakList> mSkinViews = new WeakHashMap<>();

    private final WeakHashMap<Context, List<SkinThemeAttr>> mSkinThemeAttrs = new WeakHashMap<>();

    private final static String TAG = "SkinViewManager";


    private static SkinViewManager sInstance;

    private SkinViewManager() {
    }


    public static SkinViewManager getInstance() {
        if (sInstance == null) {
            synchronized (SkinViewManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinViewManager();
                }
            }
        }

        return sInstance;
    }


    public void applySkin() {
        Set<Map.Entry<Context, SkinViewWeakList>> entries = mSkinViews.entrySet();

        SkinResources resources = null;

        for (Map.Entry<Context, SkinViewWeakList> entry : entries) {

            final SkinViewWeakList skinViews = entry.getValue();

            for (SkinView view : skinViews) {
                if (view != null && view.getView() != null) {
                    View v = view.getView();

                    //判断是否是重写view实现的换肤
                    if (v instanceof SkinViewChanger && ((SkinViewChanger) v).skinEnabled()) {
                        if (resources == null) {
                            resources = SkinManager.getInstance().getResources(v.getContext());
                        }

                        ((SkinViewChanger) v)
                                .onSkinChanged(new SkinViewChanger.Helper(v, view.getAttrs())
                                        , resources, v.getContext().getTheme());
                    } else {
                        final List<SkinViewAttr> attrs = view.getAttrs();

                        if (attrs != null) {
                            for (SkinViewAttr attr : view.getAttrs()) {
                                attr.apply(view.getView());
                            }
                        }
                    }
                }
            }
        }

        //应用主题
        final Set<Map.Entry<Context, List<SkinThemeAttr>>> entrySet = mSkinThemeAttrs.entrySet();

        for (Map.Entry<Context, List<SkinThemeAttr>> entry : entrySet) {
            final Activity activity = ContextUtils.getActivityByContext(entry.getKey());
            final List<SkinThemeAttr> attrs = entry.getValue();
            if (activity != null) {
                for (SkinThemeAttr attr : attrs) {
                    attr.apply(activity);
                }
            }
        }
    }


    boolean addSkinView(View view, List<SkinViewAttr> attrs) {
        Context context = view.getContext();

        Activity activity = ContextUtils.getActivityByContext(context);

        if (activity == null) {
            return false;
        } else {
            SkinViewWeakList views = mSkinViews.get(activity);
            if (views == null) {
                views = new SkinViewWeakList();
                mSkinViews.put(activity, views);
            }

            views.add(view, attrs);

            return true;
        }
    }


    void loadSkinThemeAttrs(Context context, SkinViewInflaterFactory factory) {
        final Activity activity = ContextUtils.getActivityByContext(context);

        int themeAttrArr[] = SkinManager.getInstance().getSkinThemeAttrArr(factory);
        if (themeAttrArr.length > 0) {
            final TypedArray a = context.obtainStyledAttributes(themeAttrArr);


            boolean hasSkin = SkinManager.getInstance().getCurrentSkin() != null;

            List<SkinThemeAttr> skinThemeAttrs = null;

            for (int i = 0; i < themeAttrArr.length; i++) {
                int resourceId = a.getResourceId(i, 0);

                if (resourceId != 0) {
                    int attrId = themeAttrArr[i];
                    SkinThemeApplicator applicator = SkinManager.getInstance().getSkinThemeApplicator(factory,
                            attrId);
                    SkinThemeAttr attr = new SkinThemeAttr(attrId, resourceId, applicator);

                    if (skinThemeAttrs == null) {
                        skinThemeAttrs = new LinkedList<>();
                    }

                    skinThemeAttrs.add(attr);

                    if (hasSkin) {
                        attr.apply(activity);
                    }
                }
            }

            a.recycle();

            if (skinThemeAttrs != null) {
                mSkinThemeAttrs.put(context, skinThemeAttrs);
            }
        }
    }
}
