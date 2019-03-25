package cn.fxlcy.fix;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.fxlcy.simpleskin.DefaultSkinGlobalConfigRegister;
import cn.fxlcy.simpleskin.SkinApplicator;
import cn.fxlcy.simpleskin.SkinManager;
import cn.fxlcy.simpleskin.SkinResources;
import cn.fxlcy.simpleskin.ViewType;

public class SkinGlobalRegister extends DefaultSkinGlobalConfigRegister {

    @Override
    public void register(SkinManager skinManager) {
        skinManager.registerGlobalSkinApplicator(new ViewType(View.class, true), new SkinApplicator<View>() {
            protected int[] attrIds() {
                return new int[]{16842964};
            }

            protected void apply(View view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case 16842964:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.setBackground(resources.getDrawable(value, theme));
                        } else {
                            view.setBackgroundDrawable(resources.getDrawable(value, theme));
                        }
                    default:
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType(TextView.class, true), new SkinApplicator<TextView>() {
            protected int[] attrIds() {
                return new int[]{16842904, 16842901, 16843087, 16843088, 16842906};
            }

            protected void apply(TextView view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case 16842901:
                        view.setTextSize(0, resources.getDimension(value));
                        break;
                    case 16842904:
                        view.setTextColor(resources.getColorStateList(value, theme));
                        break;
                    case 16842906:
                        view.setHintTextColor(resources.getColorStateList(value, theme));
                        break;
                    case 16843087:
                        view.setText(resources.getText(value));
                        break;
                    case 16843088:
                        view.setHint(resources.getText(value));
                }

            }
        });
        skinManager.registerGlobalSkinApplicator(new ViewType(ImageView.class, true), new SkinApplicator<ImageView>() {
            protected int[] attrIds() {
                return new int[]{16843033};
            }

            protected void apply(ImageView view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case 16843033:
                        view.setImageDrawable(resources.getDrawable(value, theme));
                    default:
                }
            }
        });
        skinManager.registerGlobalSkinApplicator(new ViewType(CompoundButton.class, true), new SkinApplicator<CompoundButton>() {
            protected int[] attrIds() {
                return new int[]{16843015};
            }

            protected void apply(CompoundButton view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case 16843015:
                        view.setButtonDrawable(resources.getDrawable(value, theme));
                    default:
                }
            }
        });

    }
}
