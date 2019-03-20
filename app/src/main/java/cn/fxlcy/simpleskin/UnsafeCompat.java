package cn.fxlcy.simpleskin;

import sun.misc.Unsafe;

public class UnsafeCompat {

    private static Unsafe theUnsafe;

    public static Unsafe getUnSafe() {
        if (theUnsafe == null) {
            synchronized (UnsafeCompat.class) {
                try {
                    theUnsafe = (Unsafe) Unsafe.class.getDeclaredField("theUnsafe")
                            .get(null);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        return theUnsafe;
    }
}
