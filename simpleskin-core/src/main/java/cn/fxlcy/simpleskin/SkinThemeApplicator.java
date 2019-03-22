package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.content.res.Resources;

public abstract class SkinThemeApplicator {


    final void apply(Activity activity, int attr, int value) {
        apply(activity, SkinManager.getInstance().getResources(activity), activity.getTheme(), attr, value);
    }


    protected abstract void apply(Activity activity, SkinResources resources, Resources.Theme theme, int attr, int value);

}
