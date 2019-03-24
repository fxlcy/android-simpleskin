package cn.fxlcy.simpleskin;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

final class DefaultSkinConfigGlobalRegister implements SkinConfigGlobalRegister {

    @Override
    public void register(SkinManager skinManager) {
        skinManager.registerGlobalSkinApplicator(new ViewType<>(View.class, true), new SkinApplicator<View>() {
            @Override
            protected int[] attrIds() {
                return new int[]{android.R.attr.background};
            }

            @Override
            protected void apply(View view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case android.R.attr.background:
                        ViewCompat.setBackground(view,
                                resources.getDrawable(value, theme));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(TextView.class, true), new SkinApplicator<TextView>() {
            @Override
            protected int[] attrIds() {
                return new int[]{android.R.attr.textColor, android.R.attr.textSize
                        , android.R.attr.text, android.R.attr.hint, android.R.attr.textColorHint};
            }

            @Override
            protected void apply(TextView view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case android.R.attr.textColor:
                        view.setTextColor(resources.getColorStateList(value, theme));
                        break;
                    case android.R.attr.textSize:
                        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(value));
                        break;
                    case android.R.attr.text:
                        view.setText(resources.getText(value));
                        break;
                    case android.R.attr.hint:
                        view.setHint(resources.getText(value));
                        break;
                    case android.R.attr.textColorHint:
                        view.setHintTextColor(resources.getColorStateList(value, theme));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(ImageView.class, true), new SkinApplicator<ImageView>() {
            @Override
            protected int[] attrIds() {
                return new int[]{android.R.attr.src};
            }

            @Override
            protected void apply(ImageView view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case android.R.attr.src:
                        view.setImageDrawable(resources.getDrawable(value, theme));
                        break;
                }
            }
        });

        skinManager.registerGlobalSkinApplicator(new ViewType<>(CompoundButton.class, true), new SkinApplicator<CompoundButton>() {
            @Override
            protected int[] attrIds() {
                return new int[]{android.R.attr.button};
            }

            @Override
            protected void apply(CompoundButton view, SkinResources resources, Resources.Theme theme, int attrId, int value) {
                switch (attrId) {
                    case android.R.attr.button:
                        view.setButtonDrawable(resources.getDrawable(value, theme));
                        break;
                }
            }
        });

        registerTheme(skinManager);
    }


    private static void registerTheme(SkinManager skinManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SkinThemeApplicator applicator = new SkinThemeApplicator() {
                @Override
                protected void apply(Activity activity, SkinResources resources, Resources.Theme theme, int attr, int value) {
                    switch (attr) {
                        case android.R.attr.colorPrimaryDark:
                        case android.R.attr.statusBarColor:
                            activity.getWindow().setStatusBarColor(resources.getColor(value, theme));
                            break;
                        case android.R.attr.navigationBarColor:
                            activity.getWindow().setNavigationBarColor(resources.getColor(value, theme));
                            break;
                    }
                }
            };


            skinManager.registerGlobalSkinThemeApplicator(android.R.attr.colorPrimaryDark, applicator);
            skinManager.registerGlobalSkinThemeApplicator(android.R.attr.statusBarColor, applicator);
            skinManager.registerGlobalSkinThemeApplicator(android.R.attr.navigationBarColor, applicator);
        }
    }

}
