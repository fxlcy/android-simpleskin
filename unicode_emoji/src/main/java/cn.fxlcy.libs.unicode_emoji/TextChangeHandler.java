package cn.fxlcy.libs.unicode_emoji;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ReplacementSpan;

final class TextChangeHandler {
    private TextChangeHandler() {
    }

    private static boolean isSoftBankEmoji(char c) {
        return ((c >> 12) == 0xe);
    }

    static CharSequence transform(CharSequence text, int start, int count, float scale) {
        if (count <= 0 || text == null) {
            return text;
        }

        final Spannable spannable;

        final int handlerSize = start + count;

        if (text instanceof Spannable) {
            spannable = (Spannable) text;

            final EmojiconSpan[] spans = spannable.getSpans(start, handlerSize, EmojiconSpan.class);

            if (spans != null && spans.length > 0) {
                //删除已有的EmojiconSpan
                for (EmojiconSpan span : spans) {
                    spannable.removeSpan(span);
                }
            }
        } else {
            spannable = new SpannableString(text);
        }

        int skip;

        for (int i = start; i < handlerSize; i += skip) {
            skip = 0;
            Bitmap icon = null;

            char c = text.charAt(i);
            //直接拿到对应的编码
            if (isSoftBankEmoji(c)) {
                icon = UnicodeEmoji.getInstance().getEmojiBitmap(c);
                skip = icon == null ? 0 : 1;
            }

            if (icon == null) {
                //拿到占多个字符unicode
                int unicode = Character.codePointAt(text, i);
                skip = Character.charCount(unicode);

                if (unicode > 0xff) {
                    icon = UnicodeEmoji.getInstance().getEmojiBitmap(unicode);
                }

                if (i + skip < handlerSize) {
                    int followUnicode = Character.codePointAt(text, i + skip);
                    //Non-spacing mark (Combining mark)
                    if (followUnicode == 0xfe0f) {
                        int followSkip = Character.charCount(followUnicode);
                        if (i + skip + followSkip < handlerSize) {

                            int nextFollowUnicode = Character.codePointAt(text, i + skip + followSkip);
                            if (nextFollowUnicode == 0x20e3) {
                                int nextFollowSkip = Character.charCount(nextFollowUnicode);
                                Bitmap tempIcon = UnicodeEmoji.getInstance().getEmojiBitmap(unicode);

                                if (tempIcon == null) {
                                    followSkip = 0;
                                    nextFollowSkip = 0;
                                } else {
                                    icon = tempIcon;
                                }
                                skip += (followSkip + nextFollowSkip);
                            }
                        }
                    } else if (followUnicode == 0x20e3) {
                        //some older versions of iOS don't use a combining character, instead it just goes straight to the second part
                        int followSkip = Character.charCount(followUnicode);

                        Bitmap tempIcon = UnicodeEmoji.getInstance().getEmojiBitmap(unicode);
                        if (tempIcon == null) {
                            followSkip = 0;
                        } else {
                            icon = tempIcon;
                        }
                        skip += followSkip;

                    }
/*                    else {
                        int followSkip = Character.charCount(followUnicode);

                        Bitmap tempIcon = UnicodeEmoji.getInstance().getEmojiBitmap(unicode);

                        if (tempIcon == null) {
                            followSkip = 0;
                        } else {
                            icon = tempIcon;
                        }

                        skip += followSkip;
                    }*/
                }
            }


            if (icon != null) {
                spannable.setSpan(new EmojiconSpan(icon, scale), i, i + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannable;
    }

    static void onTextChanged(Spannable text, int start, int count, float scale) {
        transform(text, start, count, scale);
    }


    private final static class EmojiconSpan extends ReplacementSpan {
        private final Bitmap mBitmap;

        private final Rect mSrcRect = new Rect();

        private final Rect mDstRect = new Rect();

        private final float mScale;

        private float mTranY = 0f;

        EmojiconSpan(Bitmap bitmap, float scale) {
            mBitmap = bitmap;
            mScale = scale;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();

            final float originHeight = fontMetrics.bottom - fontMetrics.top;

            final float height;

            if (mScale == 1f) {
                height = originHeight;
            } else {
                height = originHeight * mScale;

                mTranY = -(mScale - 1f) / 2f * originHeight;
            }


            int bitmapWidth = mBitmap.getWidth();
            int bitmapHeight = mBitmap.getHeight();

            int width = (int) ((float) bitmapWidth / bitmapHeight * height);


            mSrcRect.set(0, 0, bitmapWidth, bitmapHeight);
            mDstRect.set(0, 0, width, (int) height);

            return width;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            canvas.save();

            if (mScale == 1f) {
                canvas.translate(x, top);
            } else {
                canvas.translate(x, top + mTranY);
            }

            canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, paint);

            canvas.restore();
        }
    }
}
