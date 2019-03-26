package cn.fxlcy.lib.util;

import java.lang.reflect.Field;

public final class PCompat {

    public final static int VERSION_P = 28;

    private PCompat() {
    }

    private static Field mClassLoaderField;

    public static boolean compat(Class clazz) {
        if (mClassLoaderField == null) {
            synchronized (PCompat.class) {
                if (mClassLoaderField == null) {
                    try {
                        mClassLoaderField = Class.class.getDeclaredField("classLoader");
                        mClassLoaderField.setAccessible(true);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (mClassLoaderField == null) {
            return false;
        } else {
            try {
                mClassLoaderField.set(clazz, null);
                return true;
            } catch (Throwable e) {
                return false;
            }
        }

    }
}
