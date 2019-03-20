package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import cn.fxlcy.simpleskin.util.ContextUtils;


/**
 * 所有换肤的view集合管理类
 */
public class SkinViewManager {


    private WeakHashMap<Context, SkinViewWeakList> mSkinViews = new WeakHashMap<>();


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
        Collection<SkinViewWeakList> weakLinkedLists = mSkinViews.values();

        Resources resources = null;

        for (SkinViewWeakList skinViews : weakLinkedLists) {
            for (SkinView view : skinViews) {
                if (view != null && view.getView() != null) {
                    View v = view.getView();

                    //判断是否是重写view实现的换肤
                    if (v instanceof SkinViewChanger && ((SkinViewChanger) v).skinEnabled()) {
                        if (resources == null) {
                            resources = SkinManager.getInstance().getResource(v.getContext());
                        }
                        ((SkinViewChanger) v).onSkinChanged(resources, v.getContext().getTheme());
                    }

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


    public boolean addSkinView(View view, List<SkinViewAttr> attrs) {
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


}
