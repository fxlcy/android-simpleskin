package cn.fxlcy.simpleskin.util;

import android.os.Build;
import android.util.ArrayMap;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CollUtils {
    private CollUtils() {
    }


    public static <K, V> Map<K, V> newMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new ArrayMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }
}
