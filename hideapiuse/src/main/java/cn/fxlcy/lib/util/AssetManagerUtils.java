package cn.fxlcy.lib.util;

import android.content.Context;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.os.Build;

import static cn.fxlcy.lib.util.PCompat.VERSION_P;

public final class AssetManagerUtils {


    public static AssetManager createAssetManager(Context context, String path) {
        final int sdkInt = Build.VERSION.SDK_INT;
        ClassLoader classLoader = null;

        AssetManager assetManager = null;


        try {

            if (sdkInt >= VERSION_P && context.getApplicationInfo().targetSdkVersion < VERSION_P) {
                classLoader = PCompat.compat(AssetManagerUtils.class);
            }


            if (sdkInt >= VERSION_P) {
                ApkAssets apkAssets = ApkAssets.loadFromPath(path);
                assetManager = new AssetManager();
                assetManager.setApkAssets(new ApkAssets[]{apkAssets}, true);
            } else {
                assetManager = new AssetManager();
                assetManager.addAssetPath(path);
            }
        } catch (Throwable throwable) {
            try {
                if (sdkInt >= VERSION_P) {
                    assetManager = new AssetManager();
                    assetManager.addAssetPath(path);
                }
            } catch (Throwable e) {
            }
        }

        PCompat.reset(AssetManagerUtils.class, classLoader);

        return assetManager;
    }
}
