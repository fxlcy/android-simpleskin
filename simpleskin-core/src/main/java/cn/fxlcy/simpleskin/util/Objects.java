package cn.fxlcy.simpleskin.util;

public final class Objects {
    private Objects() {
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static  boolean contain(int[] arr, int value) {
        if (arr == null || arr.length == 0) {
            return false;
        }

        for (int s : arr) {
            if (Objects.equals(s, value)) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean contain(T[] arr, T value) {
        if (arr == null || arr.length == 0) {
            return false;
        }

        for (T s : arr) {
            if (Objects.equals(s, value)) {
                return true;
            }
        }

        return false;
    }
}
