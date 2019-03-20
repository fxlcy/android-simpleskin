package cn.fxlcy.simpleskin;

import android.content.res.Resources;
import android.view.View;

public abstract class SkinApplicator<T extends View> {

    private String[] mAttrs;

    protected abstract String[] getAttrs();

    protected abstract void apply(T view, Resources resources, Resources.Theme theme, String attrName, int value);

    public final String[] attrs() {
        if (mAttrs == null) {
            mAttrs = getAttrs();
        }

        return mAttrs;
    }

    public final void apply(T view, String attrName, int value) {
        apply(view, SkinManager.getInstance().getResource(view.getContext())
                , view.getContext().getTheme(), attrName, value);
    }


}
