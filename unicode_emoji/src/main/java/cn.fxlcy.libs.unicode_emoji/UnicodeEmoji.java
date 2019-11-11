package cn.fxlcy.libs.unicode_emoji;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import cn.fxlcy.layoutinflter.BaseViewInflaterFactory;

public class UnicodeEmoji {
    public final static String TAG = "UnicodeEmoji";

    private int[] mUnicodeEmojiUnicodes;
    private String mAssetsDir;

    private Context mContext;


    private volatile boolean mInitialize;

    /**
     * 默认最大可用的emoji大小
     */
    public final static float DEFAULT_MAX_TEXT_SIZE = 64f * 1.5f;

    public final static float NOT_LIMIT_TEXT_SIZE = -1f;

    public final static float DEFAULT_EMOJI_SCALE = 0.95f;

    public final static String DEFAULT_ASSETS_DIR = "unicode_emoji";

    private Config mConfig = new Config();

    //bitmap缓存
    private LruCache<Integer, Bitmap> mBitmapCache = new LruCache<Integer, Bitmap>(1024) {
        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            if (oldValue != newValue) {
                if (!oldValue.isRecycled()) {
                    try {
                        oldValue.recycle();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    };

    private final EmojiUnicodeHandler mEmojiUnicodeHandler = new EmojiUnicodeHandler() {
        private final static String PREFIX = "emoji_";

        @Override
        public Integer getUnicode(String path) {
            int prefixIndex = path.indexOf(path);
            int pointIndex = path.indexOf('.');

            if (prefixIndex >= 0 && pointIndex >= 0 && pointIndex > prefixIndex) {
                String str = path.substring(prefixIndex + PREFIX.length(), pointIndex);
                try {
                    return Integer.parseInt(str, 16);
                } catch (Throwable ignored) {
                }
            }

            return null;
        }

        @Override
        public String getPathByUnicode(int unicode) {
            return PREFIX + Integer.toHexString(unicode) + ".png";
        }
    };

    public String getAssetsPathByUnicode(int c) {
        //使用二分法查找
        if (Arrays.binarySearch(mUnicodeEmojiUnicodes, c) >= 0) {
            return mAssetsDir + "/" + mEmojiUnicodeHandler.getPathByUnicode(c);
        }

        return null;
    }

    public Bitmap getEmojiBitmap(int unicode) {

        String path = getAssetsPathByUnicode(unicode);

        if (path != null) {
            Bitmap bitmap = mBitmapCache.get(unicode);
            if (bitmap == null) {
                return loadBitmapByAssets(path, unicode);
            }

            return bitmap;
        }

        return null;
    }

    private final TextWatcher mDefaultUnicodeEmojiTextWatcher = new UnicodeEmoji.TextWatcher() {
        @Override
        public float getEmojiScale() {
            return mConfig.emojiScale;
        }
    };

    private ViewFactory mDefaultViewFactory = new ViewFactory(0, mDefaultUnicodeEmojiTextWatcher);

    {
        mDefaultViewFactory.mUnicodeEmoji = this;
    }

    public void replace(Activity activity) {
        replace(activity, null);
    }

    public void replace(Activity activity, final Config config) {
        if (config == null) {
            BaseViewInflaterFactory.addInflater(activity, mDefaultViewFactory);
        } else {
            BaseViewInflaterFactory.addInflater(activity, new ViewFactory(config.maxTextSize, new UnicodeEmoji.TextWatcher() {
                @Override
                public float getEmojiScale() {
                    return config.emojiScale;
                }
            }));
        }
    }

    public void replace(TextView textView) {
        replace(textView, null);
    }

    public void replace(TextView textView, final Config config) {
        if (config == null) {
            textView.addTextChangedListener(mDefaultUnicodeEmojiTextWatcher);
        } else {
            textView.addTextChangedListener(new UnicodeEmoji.TextWatcher() {
                @Override
                public float getEmojiScale() {
                    return config.emojiScale;
                }
            });
        }
    }


    public CharSequence transform(CharSequence text, float scale) {
        return TextChangeHandler.transform(text, 0, text == null ? 0 : text.length(), scale);
    }

    public CharSequence transform(CharSequence text) {
        return TextChangeHandler.transform(text, 0, text == null ? 0 : text.length(), mConfig.emojiScale);
    }

    public CharSequence transform(CharSequence text, int start, int count, float scale) {
        return TextChangeHandler.transform(text, start, count, scale);
    }

    private Bitmap loadBitmapByAssets(String path, int unicode) {
        InputStream is = null;

        try {
            is = mContext.getAssets().open(path);

            int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = 240;
            options.inScreenDensity = densityDpi;
            options.inTargetDensity = densityDpi;

            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

            mBitmapCache.put(unicode, bitmap);

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignored) {
                }
            }
        }

        return null;
    }

    public interface EmojiUnicodeHandler {
        Integer getUnicode(String path);

        String getPathByUnicode(int unicode);
    }

    private UnicodeEmoji() {
    }

    public void init(Context context) {
        init(context, null, null);
    }

    public void init(Context context, String assetsDir, Config config) {

        if (mInitialize) {
            return;
        }

        synchronized (this) {
            if (mInitialize) {
                return;
            }

            mInitialize = true;
        }

        String[] list = null;

        if (config != null) {
            mConfig = config;
        }

        if (assetsDir == null) {
            mAssetsDir = DEFAULT_ASSETS_DIR;
        } else {
            mAssetsDir = assetsDir;
        }
        mContext = context.getApplicationContext();

        try {
            list = context.getAssets().list(mAssetsDir);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (list != null) {
            int len = list.length;

            int[] chars = new int[list.length];

            int current = 0;

            for (String path : list) {
                Integer unicode = mEmojiUnicodeHandler.getUnicode(path);
                if (unicode != null) {
                    chars[current] = unicode;
                    current++;
                }
            }

            if (current < len) {
                chars = Arrays.copyOf(chars, current);
            }

            //排序，方便使用二分法查找
            Arrays.sort(chars);

            mUnicodeEmojiUnicodes = chars;
        }
    }

    public static UnicodeEmoji getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        @SuppressLint("StaticFieldLeak")
        private final static UnicodeEmoji INSTANCE = new UnicodeEmoji();
    }


    public static class Config {
        // maxTextSize 最大替换的文字大小
        private float maxTextSize = DEFAULT_MAX_TEXT_SIZE;

        private float emojiScale = DEFAULT_EMOJI_SCALE;

        public Config(float maxTextSize, float emojiScale) {
            this.emojiScale = emojiScale;
            this.maxTextSize = maxTextSize;
        }

        private Config() {
        }

        public void setEmojiScale(float emojiScale) {
            this.emojiScale = emojiScale;
        }

        public void setMaxTextSize(float maxTextSize) {
            this.maxTextSize = maxTextSize;
        }
    }

    private static class ViewFactory implements BaseViewInflaterFactory.Factory {

        private final UnicodeEmoji.TextWatcher mUnicodeEmojiWatcher;

        private final float mMaxTextSize;

        private UnicodeEmoji mUnicodeEmoji;


        ViewFactory(float maxTextSize, UnicodeEmoji.TextWatcher watcher) {
            this.mMaxTextSize = maxTextSize;
            this.mUnicodeEmojiWatcher = watcher;
        }

        @Override
        public View onCreateView(View parent, View view, String name, Context context, AttributeSet attrs) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;

                float maxTextSize;
                if (mUnicodeEmoji != null) {
                    maxTextSize = mUnicodeEmoji.mConfig.maxTextSize;
                } else {
                    maxTextSize = mMaxTextSize;
                }

                if (maxTextSize <= 0) {
                    textView.addTextChangedListener(mUnicodeEmojiWatcher);
                } else if (maxTextSize >= textView.getTextSize() * mUnicodeEmojiWatcher.getEmojiScale()) {
                    //过滤掉文字过大的TextView
                    textView.addTextChangedListener(mUnicodeEmojiWatcher);
                }
            }

            return view;
        }
    }


    private static abstract class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s instanceof Spannable) {
                TextChangeHandler.onTextChanged((Spannable) s, start, count, getEmojiScale());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        public abstract float getEmojiScale();
    }
}
