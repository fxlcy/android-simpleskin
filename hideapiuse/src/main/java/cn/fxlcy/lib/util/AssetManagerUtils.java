package cn.fxlcy.lib.util;

import android.content.Context;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.os.Build;

import static cn.fxlcy.lib.util.PCompat.VERSION_P;

public final class AssetManagerUtils {

    private static Boolean sCompatP = null;

    private static boolean isCompatP() {
        if (sCompatP == null) {
            synchronized (AssetManagerUtils.class) {
                if (sCompatP == null) {
                    sCompatP = PCompat.compat(AssetManagerUtils.class);
                }
            }
        }

        return sCompatP;
    }

    public static AssetManager createAssetManager(Context context, String path) {
        final int sdkInt = Build.VERSION.SDK_INT;
        try {
            if (sdkInt >= VERSION_P && (context.getApplicationInfo().targetSdkVersion < VERSION_P || isCompatP())) {
                ApkAssets apkAssets = ApkAssets.loadFromPath(path);
                AssetManager assetManager = new AssetManager();
                assetManager.setApkAssets(new ApkAssets[]{apkAssets}, true);

                return assetManager;
            } else {
                AssetManager assetManager = new AssetManager();
                assetManager.addAssetPath(path);

                return assetManager;
            }
        } catch (Throwable throwable) {
            try {
                if (sdkInt >= VERSION_P) {
                    AssetManager assetManager = new AssetManager();
                    assetManager.addAssetPath(path);

                    return assetManager;
                }
            } catch (Throwable e) {
                throw new RuntimeException("无法加载皮肤包", e);
            }


            throw new RuntimeException("无法加载皮肤包", throwable);
        }

    }
}
