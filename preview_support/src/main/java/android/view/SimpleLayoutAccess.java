package android.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.ide.common.rendering.api.LayoutlibCallback;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class SimpleLayoutAccess {


    private static Method sCommonDrawableInflaterClazzInvoke;
    private static Method sTextViewInflaterClazzInvoke;

    private static boolean sInit = false;

    private static WeakReference<LayoutlibCallback> sCallback;


    public static Class<?> findClass(String name) {
        if (sCallback != null) {
            LayoutlibCallback callback = sCallback.get();
            if (callback != null) {
                try {
                    return callback.findClass(name);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            return SimpleLayoutAccess.class.getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void init(LayoutlibCallback params) {
        sCallback = new WeakReference<>(params);

        if (sInit) return;

        sInit = true;

        try {
            Class<?> sCommonDrawableInflaterClazz = params.findClass("com.huazhen.library.simplelayout.inflater.CommonDrawableInflater");
            sCommonDrawableInflaterClazzInvoke = sCommonDrawableInflaterClazz.getMethod("inflate", View.class, Context.class, AttributeSet.class);

            Class<?> textViewInflaterClazz = params.findClass("com.huazhen.library.simplelayout.inflater.TextColorInflater");
            sTextViewInflaterClazzInvoke = textViewInflaterClazz.getMethod("inflate", Context.class, AttributeSet.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    static Drawable commonDrawableInflater(View view, Context context, AttributeSet set) {
        if (sCommonDrawableInflaterClazzInvoke != null) {
            try {
                return (Drawable) sCommonDrawableInflaterClazzInvoke.invoke(null, view, context, set);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    static ColorStateList commonTextColorInflater(Context context, AttributeSet set) {
        if (sTextViewInflaterClazzInvoke != null) {
            try {
                return (ColorStateList) sTextViewInflaterClazzInvoke.invoke(null, context, set);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
