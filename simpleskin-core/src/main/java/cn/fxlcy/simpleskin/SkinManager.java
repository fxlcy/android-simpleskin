package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.huazhen.library.simplelayout.inflater.BaseViewInflater;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.fxlcy.simpleskin.config.Constants;
import cn.fxlcy.simpleskin.util.CollUtils;
import cn.fxlcy.simpleskin.util.Objects;
import cn.fxlcy.simpleskin.util.SimpleActivityLifecycleCallbacks;
import dalvik.system.DexFile;
import dalvik.system.DexFileCompat;

public final class SkinManager {

    private final static String TAG = "SkinManager";


    private static SkinManager sInstance;

    private final SkinConfig mGlobalConfig = new SkinConfig();


    private SkinInfo mCurrentSkin;


    /**
     * 全局皮肤更改监听
     */
    private final List<OnSkinChangedListener> mGlobalSkinChangedListeners = new ArrayList<>();


    private ExecutorService mThreadPool;


    private boolean mInit = false;

    private static final Object EMPTY = new Object();

    private final WeakHashMap<Activity, Object> mActivitys = new WeakHashMap<>();

    /**
     * restoreSkin是否需要recreate activity
     */
    private boolean mNeedRecreate = false;


    public SkinInfo getCurrentSkin() {
        return mCurrentSkin;
    }


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
            sp.edit().putString(Constants.SP_SKIN_PATH, mCurrentSkin.path)
                    .putString(Constants.SP_SKIN_SCHEMA, mCurrentSkin.schema.getName())
                    .apply();
        }

        return true;
    }

    private SkinManager() {
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

    public void registerGlobalSkinWhiteAttr(int attrId) {
        mGlobalConfig.registerSkinWhiteAttr(attrId);
    }

    public void registerGlobalSkinBlackAttr(int attrId) {
        mGlobalConfig.registerSkinBlackAttr(attrId);
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


    final SkinThemeApplicator getSkinThemeApplicator(SkinViewInflaterFactory factory, int attr) {
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


    final List<SkinApplicator<? extends View>> getSkinApplicators(Class<? extends View> type, SkinViewInflaterFactory factory) {
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

        application.registerActivityLifecycleCallbacks(new SimpleActivityLifecycleCallbacks() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                mActivitys.remove(activity);
                SkinViewManager.getInstance().destroy(activity);
            }
        });

        registerConfig();

        SharedPreferences sp = application.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        String schema = sp
                .getString(Constants.SP_SKIN_SCHEMA, null);
        String path = sp.getString(Constants.SP_SKIN_PATH, null);

        SkinInfo skinInfo;

        if (TextUtils.isEmpty(schema) || TextUtils.isEmpty(path)) {
            skinInfo = null;
        } else {
            SkinInfo.Schema sc = SkinInfo.Schema.valueOfName(schema);
            if (sc != null) {
                switch (sc) {
                    case ASSETS:
                        skinInfo = SkinInfo.obtainByAssets(application, path);
                        break;
                    case FILES:
                        skinInfo = SkinInfo.obtainByLocalPath(path);
                        break;
                    default:
                        skinInfo = null;
                }
            } else {
                skinInfo = null;
            }
        }


        mInit = true;

        mCurrentSkin = skinInfo;

        //如果有皮肤，立即执行换肤操作
        if (skinInfo != null) {
            switchSkinInner(application, skinInfo, null, true);
        }


    }

    public void restoreSkin(Context context) {
        checkEnv();

        if (setCurrentSkin(context, null)) {
            applySkin(mNeedRecreate);
        }
    }

    //切换皮肤
    public void switchSkin(final Context context, final SkinInfo skinInfo, final OnSkinChangedListener listener) {
        switchSkinInner(context, skinInfo, listener, false);
    }


    //从assets中的包切换皮肤
    public void switchSkinByAssets(final Context context, final String path, final OnSkinChangedListener listener) {
        if (listener == null) {
            switchSkinInner(context, SkinInfo.obtainByAssets(context, path), null, false);
        } else {
            if (mThreadPool == null) {
                mThreadPool = Executors.newSingleThreadExecutor();
            }

            mThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    SkinInfo info = SkinInfo.obtainByAssets(context, path);
                    switchSkinInner(context, info, listener, false);
                }
            });
        }
    }

    public void installViewFactory(Activity activity) {
        installViewFactory(activity, null);
    }

    public void installViewFactory(final Activity activity, SkinConfig config) {
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


        mActivitys.put(activity, EMPTY);
    }

    @NotNull
    public SkinResources getResources(Context context) {
        return ResourcesManager.getInstance().getResources(context, mCurrentSkin);
    }


    private void registerConfig() {
        //通过ServiceLoader读取可能存在的皮肤全局配置注册器
        final Iterable<SkinGlobalConfigRegister> registers = ServiceLoader.load(SkinGlobalConfigRegister.class);
        Iterator<SkinGlobalConfigRegister> iterator = registers.iterator();
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                iterator.next().register(this);
            }

            return;
        }

        new DefaultSkinGlobalConfigRegister().register(this);

        mGlobalConfig.newImmutable();
    }

    private void applySkin(boolean needRecreate) {
        if (needRecreate) {
            SkinConfig.resetAll();
            registerConfig();
            recreateActivitys();
            mNeedRecreate = false;
        } else {
            SkinViewManager.getInstance().applySkin();
        }
    }

    private void switchSkinInner(final Context context, final SkinInfo skinInfo, final OnSkinChangedListener listener, boolean init) {
        if (init || setCurrentSkin(context, skinInfo)) {
            checkEnv();

            if (listener == null) {
                try {
                    final boolean needRecreate = mNeedRecreate;
                    if (resolveMateData(context, ResourcesManager.getInstance().getResources(context, skinInfo))) {
                        triggerListener(null, true, null);
                        recreateActivitys();
                    } else {
                        applySkin(needRecreate);
                        triggerListener(null, true, null);
                    }
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
                            applySkin((boolean) msg.obj);
                            triggerListener(listener, true, null);
                        } else if (msg.what == 1) {
                            triggerListener(listener, true, null);
                            recreateActivitys();
                        } else if (msg.what == 2) {
                            triggerListener(listener, false, (Throwable) msg.obj);
                        }
                        return false;
                    }
                });

                mThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final boolean needRecreate = mNeedRecreate;
                            if (resolveMateData(context, ResourcesManager.getInstance().getResources(context, skinInfo))) {
                                handler.sendEmptyMessage(1);
                            } else {
                                Message msg = handler.obtainMessage(0);
                                msg.obj = needRecreate;
                                msg.sendToTarget();
                            }

                        } catch (Throwable e) {
                            Message message = handler.obtainMessage(2);
                            message.obj = e;
                            message.sendToTarget();
                        }
                    }
                });
            }
        }
    }

    private void recreateActivitys() {
        Set<Activity> activities = mActivitys.keySet();
        for (Activity activity : activities) {

            if (activity != null && !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed())) {
                try {
                    activity.recreate();
                } catch (Throwable ignored) {
                }
            }

        }
    }

    private boolean resolveMateData(Context context, SkinResources resources) {
        Bundle bundle = resources.mApplicationInfo.metaData;
        if (bundle != null) {
            String registerClass = bundle.getString(Constants.METE_DATA_KEY_GLOBAL_CONFIG_REGISTER);
            DexFile dexFile = DexFileCompat.loadDexFileByApkFile(context, resources.getSkinInfo().getLocalPath(context));
            boolean reset = false;

            Map<ViewType<? extends View>, List<SkinApplicator<? extends View>>> skinApplicators = null;
            Map<Integer, SkinThemeApplicator> skinThemeApplicators = null;

            try {
                Class clazz = dexFile.loadClass(registerClass, new FixClassLoader(getClass().getClassLoader(), dexFile));
                SkinGlobalConfigRegister register = (SkinGlobalConfigRegister) clazz.newInstance();

                //重置所有skinConfig
                skinApplicators = CollUtils.newMap(mGlobalConfig.mSkinApplicator);
                skinThemeApplicators = CollUtils.newMap(mGlobalConfig.mSkinThemeApplicator);

                SkinConfig.resetAll();
                reset = true;
                register.register(this);
                mGlobalConfig.newImmutable();

                mNeedRecreate = true;
                return true;
            } catch (Throwable ex) {
                if (reset) {
                    mGlobalConfig.mSkinApplicator.putAll(skinApplicators);
                    mGlobalConfig.mSkinThemeApplicator.putAll(skinThemeApplicators);

                    mGlobalConfig.newImmutable();
                }
            }
        }


        mNeedRecreate = false;

        return false;
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


        public static SkinConfig newInstance() {
            return new SkinConfig();
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

        private final List<Integer> mSkinWhiteAttrs = new ArrayList<>();

        private final List<Integer> mSkinBlackAttrs = new ArrayList<>();

        private int[] mSkinWhiteAttrArr;

        private int[] mSkinBlackAttrArr;


        private boolean mImmutable = false;

        boolean mDefaultUse = true;


        private SkinConfig() {
        }

        public void setDefaultUseSkin(boolean defaultUse) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }

            this.mDefaultUse = defaultUse;
        }


        private static void resetAll() {
            SkinManager.getInstance().mGlobalConfig.reset();

            sSkinConfigCache.clear();
        }

        private void reset() {
            mSkinApplicatorMapCache = null;
            mSkinThemeAttrsCache = null;
            mSkinApplicator.clear();
            mSkinThemeApplicator.clear();

            mSkinWhiteAttrs.clear();
            mSkinBlackAttrs.clear();
            mSkinBlackAttrArr = null;
            mSkinWhiteAttrArr = null;

            mImmutable = false;

            mDefaultUse = true;
        }


        //置为不可变的
        final SkinConfig newImmutable() {
            mImmutable = true;
            return this;
        }


        final static SkinConfig EMPTY = new SkinConfig() {
            @Override
            int[] getWhiteAttrs() {
                return SkinManager.getInstance().mGlobalConfig.getWhiteAttrs();
            }

            @Override
            int[] getBlackAttrs() {
                return SkinManager.getInstance().mGlobalConfig.getBlackAttrs();
            }
        }.newImmutable();


        int[] getWhiteAttrs() {
            final SkinConfig globalConfig = SkinManager.getInstance().mGlobalConfig;

            if (!globalConfig.mImmutable || !mImmutable) {
                throw new UnsupportedOperationException();
            }

            if (mSkinWhiteAttrArr == null) {
                if (this == globalConfig) {
                    mSkinWhiteAttrArr = new int[this.mSkinWhiteAttrs.size()];
                    for (int i = 0; i < mSkinWhiteAttrArr.length; i++) {
                        mSkinWhiteAttrArr[i] = mSkinWhiteAttrs.get(i);
                    }
                } else {
                    mSkinWhiteAttrArr = new int[this.mSkinWhiteAttrs.size() + globalConfig.mSkinWhiteAttrs.size()];

                    final int globalSize = globalConfig.mSkinBlackAttrs.size();

                    for (int i = 0; i < mSkinWhiteAttrArr.length; i++) {
                        if (i < globalSize) {
                            mSkinWhiteAttrArr[i] = globalConfig.mSkinWhiteAttrs.get(i);
                        } else {
                            mSkinWhiteAttrArr[globalSize + i] = mSkinWhiteAttrs.get(i - globalSize);
                        }
                    }
                }

                Arrays.sort(mSkinWhiteAttrArr);
            }

            return mSkinWhiteAttrArr;
        }


        int[] getBlackAttrs() {
            final SkinConfig globalConfig = SkinManager.getInstance().mGlobalConfig;

            if (!globalConfig.mImmutable || !mImmutable) {
                throw new UnsupportedOperationException();
            }

            if (mSkinBlackAttrArr == null) {
                if (this == globalConfig) {
                    mSkinBlackAttrArr = new int[this.mSkinBlackAttrs.size()];
                    for (int i = 0; i < mSkinBlackAttrArr.length; i++) {
                        mSkinBlackAttrArr[i] = mSkinBlackAttrs.get(i);
                    }
                } else {
                    mSkinBlackAttrArr = new int[this.mSkinBlackAttrs.size() + globalConfig.mSkinBlackAttrs.size()];

                    final int globalSize = globalConfig.mSkinBlackAttrs.size();

                    for (int i = 0; i < mSkinBlackAttrArr.length; i++) {
                        if (i < globalSize) {
                            mSkinBlackAttrArr[i] = globalConfig.mSkinBlackAttrs.get(i);
                        } else {
                            mSkinBlackAttrArr[globalSize + i] = mSkinBlackAttrs.get(i - globalSize);
                        }
                    }
                }

                Arrays.sort(mSkinBlackAttrArr);
            }

            return mSkinBlackAttrArr;
        }


        public void registerSkinWhiteAttr(int attrId) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }


            synchronized (mSkinWhiteAttrs) {
                mSkinWhiteAttrs.add(attrId);
            }
        }

        public void registerSkinBlackAttr(int attrId) {
            if (mImmutable) {
                throw new UnsupportedOperationException();
            }


            synchronized (mSkinBlackAttrs) {
                mSkinBlackAttrs.add(attrId);
            }
        }


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
