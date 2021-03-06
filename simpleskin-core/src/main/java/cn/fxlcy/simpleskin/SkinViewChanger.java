package cn.fxlcy.simpleskin;

import android.content.res.Resources;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 如果想皮肤改变的逻辑都自己控制，view实现这个接口
 */
public interface SkinViewChanger {

    /**
     * 皮肤改变回调
     */
    void onSkinChanged(Helper helper, SkinResources resources, Resources.Theme theme);


    /**
     * 是否启用
     */
    boolean skinEnabled();


    final class Helper {
        private List<SkinViewAttr> mAttrs;
        private WeakReference<View> mView;

        Helper(View view, List<SkinViewAttr> attr) {
            this.mAttrs = attr;
            this.mView = new WeakReference<>(view);
        }

        public int getResourceId(int attrId, int defaultValue) {
            for (SkinViewAttr attr : mAttrs) {
                if (attr.attrId == attrId) {
                    return attr.value;
                }
            }

            return defaultValue;
        }

    }

}
