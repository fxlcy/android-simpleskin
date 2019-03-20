package cn.fxlcy.simpleskin.util;

import android.os.Build;
import android.util.ArrayMap;
import android.util.ArraySet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

    public static <T> Set<T> newSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArraySet<>();
        } else {
            return new LinkedHashSet<>();
        }
    }


    public static <T> Set<T> newSet(T... value) {
        final Set<T> set = newSet();
        set.addAll(Arrays.asList(value));
        return set;
    }
}
