package cn.fxlcy.simpleskin.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

public final class ContextUtils {
    private ContextUtils() {
    }

    public static Activity getActivityByContext(Context context) {
        Activity activity = null;
        do {
            if (context instanceof Activity) {
                activity = (Activity) context;
                break;
            }

            context = ((ContextWrapper) context).getBaseContext();
        } while (context instanceof ContextWrapper);


        return activity;
    }
}
