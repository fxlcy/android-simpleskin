package cn.fxlcy.simpleskin;

import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

final class SkinView {
    private WeakReference<View> mView;

    private List<SkinViewAttr> mAttrs;


    SkinView(View view, List<SkinViewAttr> attrs) {
        this.mView = new WeakReference<>(view);
        this.mAttrs = attrs;
    }

    public List<SkinViewAttr> getAttrs() {
        return mAttrs;
    }

    boolean valid() {
        return mView != null && mView.get() != null;
    }

    public View getView() {
        return mView.get();
    }
}
