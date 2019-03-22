package cn.fxlcy.simpleskin;

import android.app.Activity;

final class SkinThemeAttr {
    private int attrId;
    private int value;

    private SkinThemeApplicator applicator;


    SkinThemeAttr(int attrId, int value, SkinThemeApplicator applicator) {
        this.attrId = attrId;
        this.value = value;
        this.applicator = applicator;
    }


    public void apply(Activity activity) {
        applicator.apply(activity, attrId, value);
    }
}
