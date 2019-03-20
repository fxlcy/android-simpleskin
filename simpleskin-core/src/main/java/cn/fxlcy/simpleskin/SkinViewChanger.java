package cn.fxlcy.simpleskin;

import android.content.res.Resources;

/**
 * 如果想皮肤改变的逻辑都自己控制，view实现这个接口
 */
public interface SkinViewChanger {

    /**
     * 皮肤改变回调
     */
    void onSkinChanged(Resources resources, Resources.Theme theme);


    /**
     * 是否启用
     */
    boolean skinEnabled();


}
