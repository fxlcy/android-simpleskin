package com.huazhen.library.simplelayout.inflater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cn.fxlcy.layoutinflter.BaseViewInflaterFactory;
import cn.fxlcy.simplelayout.libs.R;

import static com.huazhen.library.simplelayout.inflater.CommonDrawableInflater.Type.AUTO;
import static com.huazhen.library.simplelayout.inflater.CommonDrawableInflater.Type.SELECTOR;
import static com.huazhen.library.simplelayout.inflater.CommonDrawableInflater.Type.SHAPE;


/**
 * Created by fxlcy on 18-2-7.
 */


public class CommonDrawableInflater {
    private int mStrokeWidth;
    private float mRadius;
    private float mTopLeftRadius, mTopRightRadius, mBottomRightRadius, mBottomLeftRadius;
    private int mDefaultColor = Integer.MIN_VALUE;
    private int mStrokeColor = Integer.MIN_VALUE;
    private LinkedHashMap<Integer, Integer> mStateColors;
    private LinkedHashMap<Integer, Integer> mStrokeStateColors;
    private boolean mIsRipple = true;

    private Drawable mBackground;
    private Drawable mPressedDrawable;

    private int mType = AUTO;


    private CommonDrawableInflater() {
    }

    @IntDef({AUTO, SHAPE, SELECTOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int AUTO = -1;
        int SHAPE = 0;
        int SELECTOR = 1;
    }


    public static void inject(final Activity activity) {
        BaseViewInflaterFactory.addInflater(activity, new BaseViewInflaterFactory.Factory() {
            @Override
            public View onCreateView(View parent, View view, String name, Context context, AttributeSet attrs) {
                Drawable drawable;

                if (view != null && (drawable = CommonDrawableInflater.inflate(view, context, attrs)) != null) {
                    ViewCompat.setBackground(view, drawable);
                }

                return view;
            }
        });
    }


    public static Drawable inflate(Context context, AttributeSet attrs) {
        return inflate(null, context, attrs);
    }

    public static Drawable inflate(View view, Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleLayout_CommonDrawable);


        int defaultColor = a.getColor(R.styleable.SimpleLayout_CommonDrawable_defaultColor, Integer.MIN_VALUE);
        int strokeWidth = a.getDimensionPixelSize(R.styleable.SimpleLayout_CommonDrawable_strokeWidth, 0);


        if (view != null) {
            final float pressedAlpha = a.getFloat(R.styleable.SimpleLayout_CommonDrawable_pressedAlpha, 0);

            if (pressedAlpha > 0) {
                Drawable drawable = view.getBackground();


                if (drawable instanceof BitmapDrawable) {
                    CommonDrawableInflater builder = CommonDrawableInflater.create();
                    builder.mBackground = drawable;
                    Drawable pressedDrawable = new BitmapDrawable(context.getResources()
                            , ((BitmapDrawable) drawable).getBitmap());
                    pressedDrawable.setAlpha((int) (pressedAlpha * 255));

                    builder.mPressedDrawable = pressedDrawable;

                    return builder.build();
                }

            }
        }


        //如果默认没颜色&&无边框,return
        if (defaultColor == Integer.MIN_VALUE && strokeWidth == 0) {
            a.recycle();
            return null;
        }

        CommonDrawableInflater builder = CommonDrawableInflater.create();

        builder.setDefaultColor(defaultColor)
                .setStrokeWidth(strokeWidth);

        //drawable的类型(1:selector,可变的，0:shape，正常的形状)
        int type = a.getInt(R.styleable.SimpleLayout_CommonDrawable_drawableType, AUTO);
        float radius = a.getDimension(R.styleable.SimpleLayout_CommonDrawable_radius, 0);

        builder.setType(type);
        builder.setRadius(radius);

        if (radius == 0) {
            builder.setRadius(a.getDimension(R.styleable.SimpleLayout_CommonDrawable_topLeftRadius, 0)
                    , a.getDimension(R.styleable.SimpleLayout_CommonDrawable_topRightRadius, 0)
                    , a.getDimension(R.styleable.SimpleLayout_CommonDrawable_bottomRightRadius, 0)
                    , a.getDimension(R.styleable.SimpleLayout_CommonDrawable_bottomLeftRadius, 0));
        }

        int pressedColor = a.getColor(R.styleable.SimpleLayout_CommonDrawable_pressedColor, Integer.MIN_VALUE);

        builder.putStateColor(-android.R.attr.state_enabled, a.getColor(R.styleable.SimpleLayout_CommonDrawable_noEnabledColor, Integer.MIN_VALUE))
                .putStateColor(android.R.attr.state_pressed, pressedColor)
                .putStateColor(android.R.attr.state_checked, a.getColor(R.styleable.SimpleLayout_CommonDrawable_checkedColor, Integer.MIN_VALUE))
                .putStateColor(android.R.attr.state_selected, a.getColor(R.styleable.SimpleLayout_CommonDrawable_selectedColor, Integer.MIN_VALUE))
                .putStateColor(android.R.attr.state_focused, a.getColor(R.styleable.SimpleLayout_CommonDrawable_focusedColor, Integer.MIN_VALUE));


        int strokeColor = a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokeColor, Integer.MIN_VALUE);
        builder.setStrokeDefaultColor(strokeColor);

        if (strokeWidth > 0) {
            builder.putStrokeStateColor(-android.R.attr.state_enabled, a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokeNoEnabledColor, Integer.MIN_VALUE))
                    .putStrokeStateColor(android.R.attr.state_pressed, a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokePressedColor, Integer.MIN_VALUE))
                    .putStrokeStateColor(android.R.attr.state_checked, a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokeCheckedColor, Integer.MIN_VALUE))
                    .putStrokeStateColor(android.R.attr.state_selected, a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokeSelectedColor, Integer.MIN_VALUE))
                    .putStrokeStateColor(android.R.attr.state_focused, a.getColor(R.styleable.SimpleLayout_CommonDrawable_strokeFocusedColor, Integer.MIN_VALUE));
        }

        builder.setRipple(a.getBoolean(R.styleable.SimpleLayout_CommonDrawable_ripple, true));

        a.recycle();

        return builder.build();
    }

    public static CommonDrawableInflater create() {
        return new CommonDrawableInflater();
    }

    public CommonDrawableInflater setRipple(boolean isRipple) {
        this.mIsRipple = isRipple;
        return this;
    }

    public CommonDrawableInflater setStrokeWidth(int width) {
        mStrokeWidth = width;
        return this;
    }


    public CommonDrawableInflater setRadius(float radius) {
        mRadius = radius;
        return this;
    }


    public CommonDrawableInflater setRadius(float topLeftRadius, float topRightRadius
            , float bottomRightRadius, float bottomLeftRadius) {
        mTopLeftRadius = topLeftRadius;
        mTopRightRadius = topRightRadius;
        mBottomRightRadius = bottomRightRadius;
        mBottomLeftRadius = bottomLeftRadius;

        return this;
    }

    public CommonDrawableInflater setDefaultColor(int color) {
        mDefaultColor = color;
        return this;
    }


    public CommonDrawableInflater setStrokeDefaultColor(int color) {
        mStrokeColor = color;
        return this;
    }


    public CommonDrawableInflater putStateColor(int state, int color) {
        LinkedHashMap<Integer, Integer> stateColors = mStateColors;
        if (mStateColors == null) {
            stateColors = new LinkedHashMap<>();
            mStateColors = stateColors;
        }

        stateColors.put(state, color);

        return this;
    }

    public CommonDrawableInflater putStrokeStateColor(int state, int color) {
        LinkedHashMap<Integer, Integer> stateColors = mStrokeStateColors;
        if (mStrokeStateColors == null) {
            stateColors = new LinkedHashMap<>();
            mStrokeStateColors = stateColors;
        }

        stateColors.put(state, color);

        return this;
    }

    public CommonDrawableInflater setType(@Type int type) {
        mType = type;
        return this;
    }

    @SuppressLint("NewApi")
    public Drawable build() {

        if (mPressedDrawable != null) {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, mPressedDrawable);
            stateListDrawable.addState(new int[]{}, mBackground);


            return stateListDrawable;
        }

        //如果默认没颜色&&无边框,return
        if (mDefaultColor == Integer.MIN_VALUE && mStrokeWidth == 0) {
            return null;
        }


        int type = getType(mType, mStateColors, mStrokeStateColors);
        if (type == SHAPE) {
            return getGradientDrawable(mStrokeColor, mDefaultColor);
        } else {
            boolean isRipple = getPressedColor() != Integer.MIN_VALUE && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP &&
                    mIsRipple;
            StateListDrawable stateListDrawable = new StateListDrawable();

            final LinkedHashMap<Integer, Integer> stateColors = mStateColors;
            final LinkedHashMap<Integer, Integer> strokeStateColors = mStrokeStateColors;

            int key, color, strokeColor;

            Set<Map.Entry<Integer, Integer>> set = stateColors.entrySet();
            for (Map.Entry<Integer, Integer> entry : set) {
                key = entry.getKey();
                color = entry.getValue();

                strokeColor = Integer.MIN_VALUE;

                if (strokeStateColors != null) {
                    strokeColor = strokeStateColors.get(key);
                }

                if (strokeColor != Integer.MIN_VALUE || (color != Integer.MIN_VALUE && (!isRipple || key != android.R.attr.state_pressed))) {
                    stateListDrawable.addState(new int[]{key}, getGradientDrawable(strokeColor, color));
                }
            }

            stateListDrawable.addState(new int[]{}, getGradientDrawable(mStrokeColor, mDefaultColor));

            if (isRipple) {
                return new RippleDrawable(ColorStateList.valueOf(stateColors.get(android.R.attr.state_pressed))
                        , stateListDrawable, getMaskDrawable());
            } else {
                return stateListDrawable;
            }
        }
    }


    private int getType(int type, LinkedHashMap<Integer, Integer> stateColors, LinkedHashMap<Integer, Integer> strokeStateColors) {
        //是否是shapeDrawable
        if (type == AUTO) {
            type = SHAPE;

            int s1 = 0, s2 = 0;
            if (stateColors != null && (s1 = stateColors.size()) != 0
                    || strokeStateColors != null && (s2 = strokeStateColors.size()) != 0) {
                if (s1 > 1 || s2 > 1) {
                    type = SELECTOR;
                } else {
                    if (stateColors == null || strokeStateColors == null) {
                        type = SHAPE;
                    } else {
                        Set<Integer> set = stateColors.keySet();
                        for (int i : set) {
                            if (!strokeStateColors.containsKey(i)) {
                                type = SELECTOR;
                            }
                        }
                    }
                }
            }
        }

        return type;
    }

    private int getPressedColor() {
        if (mStateColors == null) {
            return Integer.MIN_VALUE;
        }

        Integer i = mStateColors.get(android.R.attr.state_pressed);
        if (i == null) {
            return Integer.MIN_VALUE;
        } else {
            return i;
        }
    }


    private Drawable getGradientDrawable(int strokeColor, int color) {
        GradientDrawable dw = new GradientDrawable();
        int strokeWidth = mStrokeWidth;
        float radius = mRadius;
        float topLeftRadius = mTopLeftRadius;
        float topRightRadius = mTopRightRadius;
        float bottomLeftRadius = mBottomLeftRadius;
        float bottomRightRadius = mBottomRightRadius;

        if (strokeWidth > 0 && strokeColor != Integer.MIN_VALUE) {
            dw.setStroke(strokeWidth, strokeColor);
        }

        if (color != Integer.MIN_VALUE) {
            dw.setColor(color);
        }

        if (radius != 0) {
            dw.setCornerRadius(radius);
        } else if (topLeftRadius != 0 || topRightRadius != 0 || bottomRightRadius != 0 || bottomLeftRadius != 0) {
            dw.setCornerRadii(new float[]{topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius});
        }

        return dw;
    }


    private ShapeDrawable getMaskDrawable() {
        return new ShapeDrawable(new RectShape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                float radius = mRadius;
                float topLeftRadius = mTopLeftRadius;
                float topRightRadius = mTopRightRadius;
                float bottomLeftRadius = mBottomLeftRadius;
                float bottomRightRadius = mBottomRightRadius;

                final float width = this.getWidth();
                final float height = this.getHeight();
                RectF rectF = new RectF(0, 0, width, height);
                if (radius != 0f) {
                    canvas.drawRoundRect(rectF, radius, radius, paint);
                } else if (topLeftRadius != 0 || topRightRadius != 0 || bottomRightRadius != 0 || bottomLeftRadius != 0) {
                    Path path = new Path();
                    path.addRoundRect(rectF, new float[]{topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius}, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else {
                    canvas.drawRect(rectF, paint);
                }
            }
        });
    }

}
