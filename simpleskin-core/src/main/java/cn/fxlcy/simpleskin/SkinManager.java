package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.huazhen.library.simplelayout.inflater.BaseViewInflater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.fxlcy.simpleskin.config.Constants;
import cn.fxlcy.simpleskin.util.CollUtils;
import cn.fxlcy.simpleskin.util.Objects;

public class SkinManager {

    private final static String TAG = "SkinManager";


    private static SkinManager sInstance;

    private final SkinConfig mGlobalConfig;


    private SkinInfo mCurrentSkin;


    /**
     * 全局皮肤更改监听
     */
    private final List<OnSkinChangedListener> mGlobalSkinChangedListeners = new ArrayList<>();


    private ExecutorService mThreadPool;


    public SkinInfo getCurrentSkin() {
        return mCurrentSkin;
    }


    private boolean mInit = false;

    /**
     * 是否成功设置
     */
    private boolean setCurrentSkin(Context context, SkinInfo currentSkin) {
        if (Objects.equals(this.mCurrentSkin, currentSkin)) {
            return false;
        }

        this.mCurrentSkin = currentSkin;

        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);

        if (currentSkin == null) {
            sp.edit().clear().apply();
        } else {
            sp.edit().putString(Constants.SP_SKIN_PATh, mCurrentSkin.getPath())
                    .putString(Constants.SP_SKIN_KEY, mCurrentSkin.getSkinName())
                    .apply();
        }

        return true;
    }

    private SkinManager() {
        mGlobalConfig = new SkinConfig();

        DefaultSkinApplicatorRegister.register(this);

    }


    public static SkinManager getInstance() {
        if (sInstance == null) {
            synchronized (SkinManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinManager();
                }
            }
        }

        return sInstance;
    }


    public void registerGlobalSkinChangeListener(OnSkinChangedListener l) {
        synchronized (mGlobalSkinChangedListeners) {
            mGlobalSkinChangedListeners.add(l);
        }
    }

    public void unregisterGlobalSkinChangeListener(OnSkinChangedListener l) {
        synchronized (mGlobalSkinChangedListeners) {
            mGlobalSkinChangedListeners.remove(l);
        }
    }

    public <T extends View> void registerGlobalSkinApplicator(ViewType<T> type, SkinApplicator<? extends View> skinApplicator) {
        mGlobalConfig.registerSkinApplicator(type, skinApplicator);
    }

    public void registerGlobalSkinThemeApplicator(int attr, SkinThemeApplicator skinThemeApplicator) {
        mGlobalConfig.registerSkinThemeApplicator(attr, skinThemeApplicator);
    }

    final int[] getSkinThemeAttrArr(SkinViewInflaterFactory factory) {
        final SkinConfig config = factory.mConfig;

        boolean global = config == SkinConfig.EMPTY || config.mSkinThemeApplicator.size() == 0;
        SkinConfig currentConfig = global ? mGlobalConfig : config;

        int[] attrs = currentConfig.mSkinThemeAttrsCache;

        if (attrs == null) {

            Set<Integer> set = mGlobalConfig.mSkinThemeApplicator.keySet();

            if (!global) {
                set = new HashSet<>(set);
                set.addAll(config.mSkinThemeApplicator.keySet());
            }


            attrs = new int[set.size()];
            int i = 0;
            for (Integer integer : set) {
                attrs[i] = integer;
                i++;
            }

            Arrays.sort(attrs);

            currentConfig.mSkinThemeAttrsCache = attrs;
        }

        return attrs;
    }


    SkinThemeApplicator getSkinThemeApplicator(SkinViewInflaterFactory factory, int attr) {
        SkinConfig config = factory.mConfig;

        if (config != SkinConfig.EMPTY) {
            SkinThemeApplicator applicator = config.mSkinThemeApplicator.get(attr);
            if (applicator != null) {
                return applicator;
            } else {
                return mGlobalConfig.mSkinThemeApplicator.get(attr);
            }
        } else {
            return mGlobalConfig.mSkinThemeApplicator.get(attr);
        }
    }


    List<SkinApplicator<? extends View>> getSkinApplicators(Class<? extends View> type, SkinViewInflaterFactory factory) {
        SkinConfig config = factory.mConfig;

        List<SkinApplicator<? extends View>> skinApplicators;

        if (config != SkinConfig.EMPTY) {
            skinApplicators = getSkinApplicatorsWithCache(type, config);
            skinApplicators.addAll(getSkinApplicatorsWithCache(type, mGlobalConfig));
        } else {
            skinApplicators = getSkinApplicatorsWithCache(type, mGlobalConfig);
        }

        return skinApplicators;
    }


    private List<SkinApplicator<? extends View>> getSkinApplicatorsWithCache(Class<? extends View> type, SkinConfig config) {
        Map<Class<? extends View>, List<SkinApplicator<? extends View>>> value = config.mSkinApplicatorMapCache;
        List<SkinApplicator<? extends View>> list;

        if (value == null) {
            value = CollUtils.newMap();
            config.mSkinApplicatorMapCache = value;
            list = new LinkedList<>();
            value.put(type, list);
        } else {
            list = value.get(type);
            if (list == null) {
                list = new LinkedList<>();
                value.put(type, list);
            } else {
                return list;
            }
        }

        Set<Map.Entry<ViewType<? extends View>, List<SkinApplicator<? extends View>>>> entries =
                config.mSkinApplicator.entrySet();


        for (Map.Entry<ViewType<? extends View>, List<SkinApplicator<? extends View>>> entry : entries) {
            ViewType<? extends View> viewType = entry.getKey();
            if (viewType.conform(type)) {
                list.addAll(entry.getValue());
            }
        }

        return list;
    }


    //初始化
    public void init(Application application) {
        if (mInit) {
            throw new RuntimeException("already initialized");
        }

        SharedPreferences sp = application.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        String name = sp
                .getString(Constants.SP_SKIN_KEY, null);
        String path = sp.getString(Constants.SP_SKIN_PATh, null);

        SkinInfo skinInfo;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(path)) {
            skinInfo = null;
        } else {
            skinInfo = new SkinInfo(name, path);
        }


        mInit = true;
        mGlobalConfig.newImmutable();

        mCurrentSkin = skinInfo;

        //如果有皮肤，立即执行换肤操作
        if (skinInfo != null) {
            switchSkinInner(application, skinInfo, null, true);
        }


    }

    public void restoreSkin(Context context) {
        checkEnv();

        if (setCurrentSkin(context, null)) {
            SkinViewManager.getInstance().applySkin();
        }
    }

    //切换皮肤
    public void switchSkin(final Context context, final SkinInfo skinInfo, final OnSkinChangedListener listener) {
        switchSkinInner(context, skinInfo, listener, false);
    }


    public void installInflater(Activity activity) {
        installInflater(activity, null);
    }

    public void installInflater(final Activity activity, SkinConfig config) {
        final SkinViewInflaterFactory factory2;
        if (config == null) {
            factory2 = SkinViewInflaterFactory.getDefault();
        } else {
            SkinConfig skinConfig = config.newImmutable();
            factory2 = new SkinViewInflaterFactory(skinConfig);
        }

        BaseViewInflater.addInflater(activity, factory2);

        Window window = activity.getWindow();
        if (window != null) {
            window.getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    SkinViewManager.getInstance().loadSkinThemeAttrs(activity, factory2);
                }
            });
        } else {
            SkinViewManager.getInstance().loadSkinThemeAttrs(activity, factory2);
        }

    }


    public SkinResources getResources(Context context) {
        if (mCurrentSkin == null) {
            return SkinResources.getSkinResource(context);
        } else {
            return ResourcesManager.getInstance().getResources(context, mCurrentSkin);
        }
    }


    private void switchSkinInner(final Context context, final SkinInfo skinInfo, final OnSkinChangedListener listener, boolean init) {
        if (init || setCurrentSkin(context, skinInfo)) {
            checkEnv();

            if (listener == null) {
                try {
                    ResourcesManager.getInstance().getResources(context, skinInfo);
                    SkinViewManager.getInstance().applySkin();
                    triggerListener(null, true, null);
                } catch (Throwable e) {
                    triggerListener(null, false, e);
                }
            } else {

                if (mThreadPool == null) {
                    mThreadPool = Executors.newSingleThreadExecutor();
                }

                final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        if (msg.what == 0) {
                            SkinViewManager.getInstance().applySkin();
                            triggerListener(listener, true, null);
                        } else if (msg.what == 1) {
                            triggerListener(listener, false, (Throwable) msg.obj);
                        }
                        return false;
                    }
                });

                mThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ResourcesManager.getInstance().getResources(context, skinInfo);
                            handler.sendEmptyMessage(0);
                        } catch (Throwable e) {
                            Message message = Message.obtain();
                            message.obj = e;
                            handler.sendMessage(message);
                        }
                    }
                });
            }
        }
    }


    private void checkEnv() {
        if (!mInit) {
            throw new RuntimeException("请先执行初始化方法init(Application app)");
        }

        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new RuntimeException("只能在主线程切换皮肤");
        }

    }

    private void triggerListener(OnSkinChangedListener listener, boolean succ, Throwable e) {
        Object[] ls = null;

        synchronized (mGlobalSkinChangedListeners) {
            if (mGlobalSkinChangedListeners.size() > 0) {
                ls = mGlobalSkinChangedListeners.toArray();
            }
        }

        if (succ) {
            if (listener != null) {
                listener.onSuccess();
            }

            if (ls != null) {
                for (Object obj : ls) {
                    ((OnSkinChangedListener) obj).onSuccess();
                }
            }
        } else {
            if (listener != null) {
                listener.onFail(e);
            }

            if (ls != null) {
                for (Object obj : ls) {
                    ((OnSkinChangedListener) obj).onFail(e);
                }
            }
        }
    }


    public interface OnSkinConfigInitializer {
        SkinConfig init(SkinConfig skinConfig);
    }


    public static class SkinConfig {

        public static SkinConfig obtain(Activity activity, OnSkinConfigInitializer initializer) {
            SkinConfig skinConfig = sSkinConfigCache.get(activity.getClass());
            if (skinConfig == null) {
                skinConfig = new SkinConfig();
                skinConfig = initializer.init(skinConfig);
                sSkinConfigCache.put(activity.getClass(), skinConfig);
            }

            return skinConfig;
        }

        private final static Map<Class<?>, SkinConfig> sSkinConfigCache = CollUtils.newMap();


        /**
         * viewtype和SkinApplicator的映射关系缓存
         */
        Map<Class<? extends View>, List<SkinApplicator<? extends View>>> mSkinApplicatorMapCache;

        int[] mSkinThemeAttrsCache;

        private final Map<ViewType<? extends View>, List<SkinApplicator<? extends View>>> mSkinApplicator
                = CollUtils.newMap();

        private final Map<Integer, SkinThemeApplicator> mSkinThemeApplicator = CollUtils.newMap();

        private boolean mImmutable = false;

        boolean mDefaultUse = true;

        public void setDefaultUseSkin(boolean defaultUse) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }

            this.mDefaultUse = defaultUse;
        }


        //置为不可变的
        final SkinConfig newImmutable() {
            mImmutable = true;
            return this;
        }


        final static SkinConfig EMPTY = new SkinConfig().newImmutable();


        public void registerSkinThemeApplicator(int attr, SkinThemeApplicator skinThemeApplicator) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }

            synchronized (mSkinThemeApplicator) {
                mSkinThemeApplicator.put(attr, skinThemeApplicator);
            }
        }

        public <T extends View> void registerSkinApplicator(ViewType<T> type, SkinApplicator<? extends View> skinApplicator) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }

            synchronized (mSkinApplicator) {
                List<SkinApplicator<? extends View>> skinApplicators = mSkinApplicator.get(type);

                if (skinApplicators == null) {
                    skinApplicators = new LinkedList<>();
                    mSkinApplicator.put(type, skinApplicators);
                }

                skinApplicators.add(0, skinApplicator);
            }
        }

    }
}
