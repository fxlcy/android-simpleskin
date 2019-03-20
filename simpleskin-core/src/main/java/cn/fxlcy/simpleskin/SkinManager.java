package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.huazhen.library.simplelayout.inflater.BaseViewInflater;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.fxlcy.simpleskin.config.Constants;
import cn.fxlcy.simpleskin.util.CollUtils;

public class SkinManager {

    private static SkinManager sInstance;

    private SkinConfig mGlobalConfig;


    private SkinInfo mCurrentSkin;


    /**
     * viewtype和SkinApplicator的映射关系缓存
     */
    private final WeakHashMap<SkinConfig, Map<Class<? extends View>, List<SkinApplicator<? extends View>>>> mSkinApplicatorMapCache
            = new WeakHashMap<>();


    /**
     * 全局皮肤更改监听
     */
    private final List<OnSkinChangedListener> mGlobalSkinChangedListeners = new ArrayList<>();


    private ExecutorService mThreadPool;


    public SkinInfo getCurrentSkin() {
        return mCurrentSkin;
    }


    public void setCurrentSkin(Context context, SkinInfo currentSkin) {
        this.mCurrentSkin = currentSkin;

        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);

        if (currentSkin == null) {
            sp.edit().clear().apply();
        } else {
            sp.edit().putString(Constants.SP_SKIN_PATh, mCurrentSkin.getPath())
                    .putString(Constants.SP_SKIN_KEY, mCurrentSkin.getSkinName())
                    .apply();
        }
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
            mGlobalSkinChangedListeners.add(0, l);
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
        Map<Class<? extends View>, List<SkinApplicator<? extends View>>> value = mSkinApplicatorMapCache.get(config);
        List<SkinApplicator<? extends View>> list;

        if (value == null) {
            value = CollUtils.newMap();
            mSkinApplicatorMapCache.put(config, value);
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

        //如果有皮肤，立即执行换肤操作
        if (skinInfo != null) {
            switchSkin(application, skinInfo, null);
        }
    }

    public void restoreSkin(Context context) {
        checkThread();

        setCurrentSkin(context, null);

        SkinViewManager.getInstance().applySkin();
    }

    //切换皮肤
    public void switchSkin(final Context context, final SkinInfo skinInfo, final OnSkinChangedListener listener) {
        checkThread();

        setCurrentSkin(context, skinInfo);

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

    public Resources getResource(Context context) {
        if (mCurrentSkin == null) {
            return context.getResources();
        } else {
            return ResourcesManager.getInstance().getResources(context, mCurrentSkin);
        }
    }

    private void checkThread() {
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

    public void installInflater(Activity activity) {
        installInflater(activity, null);
    }

    public void installInflater(Activity activity, SkinConfig config) {
        SkinViewInflaterFactory factory2;
        if (config == null) {
            factory2 = SkinViewInflaterFactory.getDefault();
        } else {
            factory2 = new SkinViewInflaterFactory(config);
        }

        BaseViewInflater.addInflater(activity, factory2);
    }


    public static class SkinConfig {

        private final Map<ViewType<? extends View>, List<SkinApplicator<? extends View>>> mSkinApplicator
                = CollUtils.newMap();

        boolean defaultUse = true;


        public void setDefaultUseSkin(boolean defaultUse) {
            this.defaultUse = defaultUse;
        }

        final static SkinConfig EMPTY = new SkinConfig() {
            @Override
            public <T extends View> void registerSkinApplicator(ViewType<T> type, SkinApplicator<? extends View> skinApplicator) {
                throw new UnsupportedOperationException();
            }
        };

        public <T extends View> void registerSkinApplicator(ViewType<T> type, SkinApplicator<? extends View> skinApplicator) {
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
