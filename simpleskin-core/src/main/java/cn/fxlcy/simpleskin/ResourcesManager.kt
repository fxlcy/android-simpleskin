package cn.fxlcy.simpleskin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import cn.fxlcy.lib.util.AssetManagerUtils
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("PrivateApi")
class ResourcesManager private constructor() {
    private val mResources = ConcurrentHashMap<String, WeakReference<SkinResources>>()

    private val mCacheResources = ConcurrentHashMap<String, SoftReference<SkinResources>>()

    private val mReferenceQueue = ReferenceQueue<SkinResources>()


    private fun expungeStaleEntries() {
        var x: Reference<*>?

        do {
            x = mReferenceQueue.poll()

            x?.get()?.apply {
                val skinName = mSkinInfo.skinName
                mResources.remove(skinName)
                mCacheResources[skinName] = SoftReference(this)
            }
        } while (x != null)
    }


    fun getResources(context: Context, skinInfo: SkinInfo): SkinResources {

        synchronized(mResources) {

            expungeStaleEntries()

            val skinName = skinInfo.skinName
            val path = skinInfo.path

            val weakRef = mResources[skinName]

            if (weakRef != null) {
                val resources = weakRef.get()
                if (resources != null) {
                    return resources
                }
            }


            val softRef = mCacheResources[skinName]
            if (softRef != null) {
                val resources = softRef.get()
                if (resources != null) {
                    mResources[resources.mSkinInfo.skinName] = WeakReference(resources)
                    return resources
                }
            }


            //获取assetManager
            val assetManager = AssetManagerUtils.createAssetManager(context, path)
                    ?: throw RuntimeException("assets parsing failure")

            val resources = SkinResources(skinInfo, assetManager, context.resources, getPackageInfo(context, path))
            mResources[skinName] = WeakReference(resources)



            return resources
        }

    }


    private fun getPackageInfo(context: Context, path: String): PackageInfo {
        val pm = context.packageManager
        return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
                ?: throw IllegalArgumentException(path + "皮肤文件加载失败")
    }


    companion object {

        private var sInstance: ResourcesManager? = null


        @JvmStatic
        val instance: ResourcesManager
            get() {
                if (sInstance == null) {
                    synchronized(ResourcesManager::class.java) {
                        if (sInstance == null) {
                            sInstance = ResourcesManager()
                        }
                    }
                }

                return sInstance!!
            }
    }
}
