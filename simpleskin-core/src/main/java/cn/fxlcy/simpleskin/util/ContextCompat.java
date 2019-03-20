package cn.fxlcy.simpleskin.util;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

public final class ContextCompat {
    private ContextCompat() {
    }


    public static Drawable getDrawable(Resources resources, Resources.Theme theme, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return resources.getDrawable(id, theme);
        } else {
            return resources.getDrawable(id);
        }
    }

    public static ColorStateList getColor(Resources resources, Resources.Theme theme, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColorStateList(id, theme);
        } else {
            return resources.getColorStateList(id);
        }
    }
}
