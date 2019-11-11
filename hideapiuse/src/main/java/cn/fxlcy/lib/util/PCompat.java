package cn.fxlcy.lib.util;

import java.lang.reflect.Field;

public final class PCompat {

    public final static int VERSION_P = 28;

    private PCompat() {
    }

    private static Field sClassLoaderField;


    private static void findField() {
        if (sClassLoaderField == null) {
            synchronized (PCompat.class) {
                if (sClassLoaderField == null) {
                    try {
                        sClassLoaderField = Class.class.getDeclaredField("classLoader");
                        sClassLoaderField.setAccessible(true);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

    }

    public static ClassLoader compat(Class clazz) {

        findField();

        if (sClassLoaderField == null) {
            return null;
        } else {
            try {
                ClassLoader classLoader = (ClassLoader) sClassLoaderField.get(clazz);
                sClassLoaderField.set(clazz, null);
                return classLoader;
            } catch (Throwable e) {
                return null;
            }
        }

    }

    public static void reset(Class clazz, ClassLoader classLoader) {
        if (classLoader != null) {
            findField();

            if (sClassLoaderField != null) {
                try {
                    sClassLoaderField.set(clazz, classLoader);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
