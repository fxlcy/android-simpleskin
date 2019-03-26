package com.huazhen.library.simplelayout.inflater;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cn.fxlcy.layoutinflter.BaseViewInflaterFactory;
import cn.fxlcy.simplelayout.libs.R;

/**
 * Created by fxlcy on 18-2-28.
 */

public class TextColorInflater {

    private TextColorInflater() {
    }

    public static void inject(Activity activity) {
        BaseViewInflaterFactory.addInflater(activity, new BaseViewInflaterFactory.Factory() {
            @Override
            public View onCreateView(View parent, View view, String name, Context context, AttributeSet attrs) {
                if (view instanceof TextView) {
                    ColorStateList colorStateList = TextColorInflater.inflater(context, attrs);
                    if (colorStateList != null) {
                        ((TextView) view).setTextColor(colorStateList);
                    }
                }

                return view;
            }
        });
    }

    public static ColorStateList inflater(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleLayout_TextView);

        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        int color;
        boolean hasDefaultColor = false;


        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_textNoEnabledColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(-android.R.attr.state_enabled);
            values.add(color);
        }

        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_textPressedColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(android.R.attr.state_pressed);
            values.add(color);
        }

        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_textCheckedColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(android.R.attr.state_checked);
            values.add(color);
        }

        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_textSelectedColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(android.R.attr.state_selected);
            values.add(color);
        }

        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_textFocusedColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(android.R.attr.state_focused);
            values.add(color);
        }

        if ((color = ta.getColor(R.styleable.SimpleLayout_TextView_android_textColor, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            keys.add(Integer.MIN_VALUE);
            values.add(color);
            hasDefaultColor = true;
        }


        ta.recycle();

        int size = keys.size();

        if (!(size > 1 || (size > 0 && !hasDefaultColor))) {
            return null;
        }


        int[][] status = new int[size][];
        int[] colors = new int[size];


        for (int i = 0; i < size; i++) {
            int key = keys.get(i);
            int value = values.get(i);

            if (key == Integer.MIN_VALUE) {
                status[i] = new int[]{};
            } else {
                status[i] = new int[]{key};
            }

            colors[i] = value;

        }


        return new ColorStateList(status, colors);
    }
}
