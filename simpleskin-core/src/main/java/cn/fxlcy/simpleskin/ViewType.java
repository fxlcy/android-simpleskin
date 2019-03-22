package cn.fxlcy.simpleskin;

import android.view.View;

public final class ViewType<T extends View> {
    private Class<T> type;

    private boolean inherit;


    public ViewType(Class<T> type, boolean inherit) {
        this.type = type;
        this.inherit = inherit;
    }

    public Class<? extends View> getType() {
        return type;
    }

    public boolean isInherit() {
        return inherit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewType) {
            return ((ViewType) obj).type == type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (type.getName() + inherit).hashCode() / 2 + 0xFFF;
    }


    boolean conform(Class<? extends View> type) {
        if (inherit) {
            return this.type.isAssignableFrom(type);
        } else {
            return this.type == type;
        }
    }
}
