package cn.fxlcy.simpleskin;

import android.view.View;

public class SkinViewAttr {
    String attrName;
    int value;

    SkinApplicator applicator;

    public int getValue() {
        return value;
    }

    public String getAttrName() {
        return attrName;
    }

    @SuppressWarnings("unchecked")
    public void apply(View view) {
        applicator.apply(view, attrName, value);
    }
}
