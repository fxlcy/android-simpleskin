package cn.fxlcy.simpleskin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import cn.fxlcy.lib.util.AssetManagerUtils
import java.io.File
import java.lang.ref.SoftReference


@SuppressLint("PrivateApi")
class ResourcesManager private constructor() {
    private var mCurrentResources: SkinResources? = null
    private var mCurrentSkinInfo: SkinInfo? = null

    private val mCacheResources = HashMap<SkinInfo, SoftReference<SkinResources>>()

    @Synchronized
    fun getResources(context: Context, skinInfo: SkinInfo?): SkinResources {
        var si = skinInfo
        val cr = mCurrentResources
        val csi = mCurrentSkinInfo

        if (si == null) {
            if (cr != null && csi != null) {
                mCacheResources[csi] = SoftReference(cr)
                mCurrentSkinInfo = null
                mCurrentResources = null
            }

            return SkinResources.getSkinResource(context)
        }


        if (mCurrentSkinInfo == si
                && cr != null) {
            return cr
        }

        val softRef = mCacheResources.remove(si)
        if (softRef != null) {
            val resources = softRef.get()
            if (resources != null) {
                val iterator = mCacheResources.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().value.get() == null) {
                        iterator.remove()
                    }
                }

                if (cr != null && csi != null) {
                    mCacheResources[csi] = SoftReference(cr)
                }

                mCurrentResources = resources
                mCurrentSkinInfo = si
                return resources
            }
        }

        val localPath = si.getLocalPath(context)
        //获取assetManager
        var am = AssetManagerUtils.createAssetManager(context, localPath)
        if (am == null && si.schema == SkinInfo.Schema.ASSETS) {
            //加载失败尝试重新解压加载
            File(localPath).delete()
            si = SkinInfo.obtainByAssets(context, si.path)
            am = AssetManagerUtils.createAssetManager(context, localPath)
        }

        if (am == null) {
            throw RuntimeException("assets parsing failure")
        }

        val resources = SkinResources.getSkinResource(am, skinInfo, context.resources, getPackageInfo(context, localPath)
                .packageName)

        if (cr != null && csi != null) {
            mCacheResources[csi] = SoftReference(cr)
        }

        mCurrentResources = resources
        mCurrentSkinInfo = si

        return resources
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
