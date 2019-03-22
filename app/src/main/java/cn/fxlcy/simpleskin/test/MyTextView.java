package cn.fxlcy.simpleskin.test;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import cn.fxlcy.simpleskin.SkinResources;
import cn.fxlcy.simpleskin.SkinViewChanger;

public class MyTextView extends View implements SkinViewChanger {

    private String text;

    private TextPaint mPaint;

    public MyTextView(Context context) {
        super(context, null);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new TextPaint();
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(32);

        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor, android.R.attr.text});

        text = a.getString(1);

        a.recycle();

        Log.i("AA", text == null ? "" : text);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(text, getWidth() / 2f, getHeight() / 2f, mPaint);
    }

    @Override
    public void onSkinChanged(Helper helper, SkinResources resources, Resources.Theme theme) {
        this.setBackground(resources.getDrawable(helper.getResourceId(android.R.attr.background, 0)));
        this.text = resources.getString(helper.getResourceId(android.R.attr.text, 0));
//        invalidate();
    }

    @Override
    public boolean skinEnabled() {
        return true;
    }
}
