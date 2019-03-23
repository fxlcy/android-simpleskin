package cn.fxlcy.simpleskin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import cn.fxlcy.lib.util.AssetManagerUtils
import java.io.File
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("PrivateApi")
class ResourcesManager private constructor() {
    private val mResources = ConcurrentHashMap<SkinInfo, WeakReference<SkinResources>>()

    private val mCacheResources = ConcurrentHashMap<SkinInfo, SoftReference<SkinResources>>()

    private val mReferenceQueue = ReferenceQueue<SkinResources>()


    private fun expungeStaleEntries() {
        var x: Reference<*>?

        do {
            x = mReferenceQueue.poll()

            x?.get()?.apply {
                mResources.remove(skinInfo)
                mCacheResources[skinInfo] = SoftReference(this)
            }
        } while (x != null)
    }


    fun getResources(context: Context, skinInfo: SkinInfo): SkinResources {

        synchronized(mResources) {

            expungeStaleEntries()

            var si = skinInfo

            val weakRef = mResources[si]

            if (weakRef != null) {
                val resources = weakRef.get()
                if (resources != null) {
                    return resources
                }
            }


            val softRef = mCacheResources[si]
            if (softRef != null) {
                val resources = softRef.get()
                if (resources != null) {
                    mResources[resources.skinInfo] = WeakReference(resources)
                    return resources
                }
            }

            val localPath = si.getLocalPath(context)
            //获取assetManager
            var am = AssetManagerUtils.createAssetManager(context, localPath)
            if (am == null && si.schema == SkinInfo.Schema.ASSETS) {
                //加载失败尝试重新解压加载
                File(localPath).delete()
                si = SkinInfo.obtainByAssets(context, skinInfo.path)
                am = AssetManagerUtils.createAssetManager(context, localPath)
            }

            if (am == null) {
                throw RuntimeException("assets parsing failure")
            }

            val resources = SkinResources.getSkinResource(am, skinInfo, context.resources, getPackageInfo(context, localPath)
                    .packageName)
            mResources[si] = WeakReference(resources)



            return resources
        }

    }


    private fun getPackageInfo(context: Context, path: String): PackageInfo {
        val pm = context.packageManager
        return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
                ?: throw IllegalArgumentException(path + "皮肤文件加载失败")
    }


    companion object {
        @JvmStatic
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
