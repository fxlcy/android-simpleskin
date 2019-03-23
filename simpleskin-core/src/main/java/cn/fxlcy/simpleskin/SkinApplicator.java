package cn.fxlcy.simpleskin;

import android.content.res.Resources;
import android.view.View;

public abstract class SkinApplicator<T extends View> {

    private int[] mAttrIds;

    protected abstract int[] attrIds();

    protected abstract void apply(T view, SkinResources resources, Resources.Theme theme, int attrId, int value);

    final int[] getAttrIds() {
        if (mAttrIds == null) {
            mAttrIds = attrIds();
        }

        return mAttrIds;
    }

    final void apply(T view,int attrId, int value) {
        apply(view, SkinManager.getInstance().getResources(view.getContext())
                , view.getContext().getTheme(), attrId, value);
    }


}
