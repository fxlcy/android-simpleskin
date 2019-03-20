package cn.fxlcy.simpleskin;

import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.fxlcy.simpleskin.util.ContextCompat;

final class DefaultSkinApplicatorRegister {


    static void register(SkinManager skinManager) {
        skinManager.registerGlobalSkinApplicator(new ViewType<>(View.class, true), new SkinApplicator<View>() {
            @Override
            protected String[] getAttrs() {
                return new String[]{"background"};
            }

            @Override
            protected void apply(View view, Resources resources, Resources.Theme theme, String attrName, int value) {
                switch (attrName) {
                    case "background":
                        ViewCompat.setBackground(view, ContextCompat.getDrawable(resources, theme, value));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(TextView.class, true), new SkinApplicator<TextView>() {
            @Override
            protected String[] getAttrs() {
                return new String[]{"textColor", "textSize", "text", "hint", "textColorHint"};
            }

            @Override
            protected void apply(TextView view, Resources resources, Resources.Theme theme, String attrName, int value) {
                switch (attrName) {
                    case "textColor":
                        view.setTextColor(ContextCompat.getColor(resources, theme, value));
                        break;
                    case "textSize":
                        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(value));
                        break;
                    case "text":
                        view.setText(resources.getText(value));
                        break;
                    case "hint":
                        view.setHint(resources.getText(value));
                        break;
                    case "textColorHint":
                        view.setHintTextColor(ContextCompat.getColor(resources, theme, value));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(ImageView.class, true), new SkinApplicator<ImageView>() {
            @Override
            protected String[] getAttrs() {
                return new String[]{"src"};
            }

            @Override
            protected void apply(ImageView view, Resources resources, Resources.Theme theme, String attrName, int value) {
                switch (attrName) {
                    case "src":
                        view.setImageDrawable(ContextCompat.getDrawable(resources, theme, value));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(CompoundButton.class, true), new SkinApplicator<CompoundButton>() {
            @Override
            protected String[] getAttrs() {
                return new String[]{"button"};
            }

            @Override
            protected void apply(CompoundButton view, Resources resources, Resources.Theme theme, String attrName, int value) {
                switch (attrName) {
                    case "button":
                        view.setButtonDrawable(ContextCompat.getDrawable(resources, theme, value));
                        break;
                }
            }
        });
    }
}
