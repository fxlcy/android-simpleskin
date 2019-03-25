package cn.fxlcy.simpleskin;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SkinResources {
    private String mPackageName;

    protected ApplicationInfo mApplicationInfo;

    private Configuration mConfiguration;
    private DisplayMetrics mDisplayMetrics;

    protected final static String TAG = "SkinResources";

    private Resources mResources;

    SkinInfo getSkinInfo() {
        return null;
    }


    public static SkinResources getSkinResource(Context context) {
        return new SkinResources(context.getResources(), context.getApplicationInfo());
    }


    public static SkinResources getSkinResource(AssetManager assetManager, SkinInfo skinInfo, Resources superResource, PackageInfo packageName) {
        return new DefaultSkinResources(assetManager, skinInfo, superResource, packageName);
    }

    private SkinResources(Resources resources, ApplicationInfo applicationInfo) {
        mConfiguration = resources.getConfiguration();
        mDisplayMetrics = resources.getDisplayMetrics();

        mApplicationInfo = applicationInfo;
        this.mPackageName = applicationInfo.packageName;
        mResources = resources;
    }

    public Resources getResources() {
        return mResources;
    }


    public final DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    public final Configuration getConfiguration() {
        return mConfiguration;
    }

    public final String getPackageName() {
        return mPackageName;
    }

    public final Drawable getDrawable(int id) throws Resources.NotFoundException {
        return getDrawable(id, null);
    }


    public final Drawable getDrawable(int id, Resources.Theme theme) throws Resources.NotFoundException {
        final int densityDpi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            densityDpi = getConfiguration().densityDpi;
        } else {
            densityDpi = getDisplayMetrics().densityDpi;
        }

        return getDrawableForDensity(id, densityDpi, theme);
    }


    public final Drawable getDrawableForDensity(int id, int density) throws Resources.NotFoundException {
        return getDrawableForDensity(id, density, null);
    }


    public Drawable getDrawableForDensity(int id, int density, Resources.Theme theme) {
        final ResourceId resourceId = getResourceId(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources(resourceId).getDrawableForDensity(resourceId.id, density, theme);
        } else {
            return getResources(resourceId).getDrawableForDensity(resourceId.id, density);
        }
    }


    public int getColor(int id, Resources.Theme theme) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources(resourceId).getColor(resourceId.id, theme);
        } else {
            return getResources(resourceId).getColor(resourceId.id);
        }
    }


    public final int getColor(int id) throws Resources.NotFoundException {
        return getColor(id, null);
    }

    @NotNull
    public ColorStateList getColorStateList(int id, Resources.Theme theme) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources(resourceId).getColorStateList(resourceId.id, theme);
        } else {
            return getResources(resourceId).getColorStateList(resourceId.id);
        }
    }


    @NotNull
    public final ColorStateList getColorStateList(int id) throws Resources.NotFoundException {
        return getColorStateList(id, null);
    }


    public float getDimension(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getDimension(resourceId.id);
    }


    public int getDimensionPixelOffset(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getDimensionPixelOffset(resourceId.id);
    }


    public int getDimensionPixelSize(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getDimensionPixelSize(resourceId.id);
    }


    @NotNull
    public CharSequence getText(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getText(resourceId.id);
    }

    public final CharSequence getText(int id, CharSequence def) {
        CharSequence res = id != 0 ? getText(id) : null;
        return res != null ? res : def;
    }

    @NotNull
    public final String getString(int id) throws Resources.NotFoundException {
        return getText(id).toString();
    }


    @NotNull
    public final String getString(int id, Object... formatArgs) throws Resources.NotFoundException {
        final String raw = getString(id);
        return format(raw, formatArgs);
    }

    @NotNull
    public CharSequence getQuantityText(int id, int quantity) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getQuantityText(resourceId.id, quantity);
    }


    @NotNull
    public final String getQuantityString(int id, int quantity) throws Resources.NotFoundException {
        return getQuantityText(id, quantity).toString();
    }


    @NotNull
    public final String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
        final String raw = getQuantityString(id, quantity);
        return format(raw, formatArgs);
    }

    @NotNull
    public CharSequence[] getTextArray(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getTextArray(resourceId.id);
    }

    @NotNull
    public String[] getStringArray(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getStringArray(resourceId.id);
    }


    public int getInteger(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getInteger(resourceId.id);
    }

    @NotNull
    public int[] getIntArray(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getIntArray(resourceId.id);
    }


    public boolean getBoolean(int id) throws Resources.NotFoundException {
        final ResourceId resourceId = getResourceId(id);
        return getResources(resourceId).getBoolean(resourceId.id);
    }


    public int getIdentifier(int id) {
        return id;
    }


    private String format(String raw, Object... formatArgs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return String.format(getConfiguration().getLocales().get(0), raw, formatArgs);
        } else {
            return String.format(Locale.getDefault(), raw, formatArgs);
        }
    }


    protected Resources getResources(ResourceId resourceId) {
        return mResources;
    }

    protected ResourceId getResourceId(int id) {
        return new ResourceId(id, false);
    }

    protected final static class ResourceId {
        private int id;
        private boolean isNew;

        private ResourceId(int id, boolean isNew) {
            this.id = id;
            this.isNew = isNew;
        }

    }


    private final static class DefaultSkinResources extends SkinResources {
        private Resources mSuperResources;

        private SkinInfo mSkinInfo;


        private DefaultSkinResources(AssetManager assetManager, SkinInfo skinInfo, Resources superResources, PackageInfo packageName) {
            super(new Resources(assetManager, superResources.getDisplayMetrics(), superResources.getConfiguration()), packageName.applicationInfo);
            mSuperResources = superResources;
            mSkinInfo = skinInfo;
        }

        @Override
        protected Resources getResources(ResourceId resourceId) {
            if (resourceId.isNew) {
                return getResources();
            } else {
                return mSuperResources;
            }
        }

        @Override
        public int getIdentifier(int id) {
            return getResourceId(id).id;
        }

        @Override
        SkinInfo getSkinInfo() {
            return mSkinInfo;
        }

        @Override
        protected ResourceId getResourceId(int id) {
            final Resources resources = mSuperResources;

            final String typeName = resources.getResourceTypeName(id);
            final String entryName = resources.getResourceEntryName(id);


            try {
                int newId = getResources().getIdentifier(entryName, typeName, getPackageName());
                if (newId == 0) {
                    return new ResourceId(id, false);
                } else {
                    return new ResourceId(newId, true);
                }
            } catch (Throwable e) {
                return new ResourceId(id, false);
            }
        }
    }
}