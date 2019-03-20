package cn.fxlcy.simpleskin;

import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SkinResources extends Resources {
    final SkinInfo mSkinInfo;
    private Resources mSuperResources;
    private PackageInfo mPackageInfo;


    public SkinResources(SkinInfo skinInfo, AssetManager assets, Resources resources, PackageInfo packageInfo) {
        super(assets, resources.getDisplayMetrics(), resources.getConfiguration());
        mSuperResources = resources;
        this.mSkinInfo = skinInfo;
        this.mPackageInfo = packageInfo;
    }


    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return getDrawable(id, null);
    }


    @Override
    public Drawable getDrawable(int id, Resources.Theme theme) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);
        if (resourceId.isNew) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return super.getDrawable(resourceId.id, theme);
            } else {
                return super.getDrawable(resourceId.id);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mSuperResources.getDrawable(resourceId.id, theme);
            } else {
                return mSuperResources.getDrawable(resourceId.id);
            }
        }
    }


    @Override
    public int getColor(int id, Resources.Theme theme) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return super.getColor(resourceId.id, theme);
            } else {
                return super.getColor(resourceId.id);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return mSuperResources.getColor(resourceId.id, theme);
            } else {
                return mSuperResources.getColor(resourceId.id);
            }
        }
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        return getColor(id, null);
    }

    @NotNull
    @Override
    public ColorStateList getColorStateList(int id, Resources.Theme theme) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return super.getColorStateList(resourceId.id, theme);
            } else {
                return super.getColorStateList(resourceId.id);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return mSuperResources.getColorStateList(resourceId.id, theme);
            } else {
                return mSuperResources.getColorStateList(resourceId.id);
            }
        }
    }


    @NotNull
    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        return getColorStateList(id, null);
    }


    @Override
    public float getDimension(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getDimension(resourceId.id);
        } else {
            return mSuperResources.getDimension(resourceId.id);
        }
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getDimensionPixelOffset(resourceId.id);
        } else {
            return mSuperResources.getDimensionPixelOffset(resourceId.id);
        }
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getDimensionPixelSize(resourceId.id);
        } else {
            return mSuperResources.getDimensionPixelSize(resourceId.id);
        }
    }


    @NotNull
    @Override
    public CharSequence getText(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getText(resourceId.id);
        } else {
            return mSuperResources.getText(resourceId.id);
        }
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        CharSequence res = id != 0 ? getText(id) : null;
        return res != null ? res : def;
    }

    @NotNull
    @Override
    public String getString(int id) throws NotFoundException {
        return getText(id).toString();
    }


    @NotNull
    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        final String raw = getString(id);
        return format(raw, formatArgs);
    }

    @NotNull
    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getQuantityText(resourceId.id, quantity);
        } else {
            return mSuperResources.getQuantityText(resourceId.id, quantity);
        }
    }


    @NotNull
    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return getQuantityText(id, quantity).toString();
    }


    @NotNull
    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        final String raw = getQuantityString(id, quantity);
        return format(raw, formatArgs);
    }

    @NotNull
    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getTextArray(resourceId.id);
        } else {
            return mSuperResources.getTextArray(resourceId.id);
        }
    }

    @NotNull
    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getStringArray(resourceId.id);
        } else {
            return mSuperResources.getStringArray(resourceId.id);
        }
    }


    @Override
    public int getInteger(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getInteger(resourceId.id);
        } else {
            return mSuperResources.getInteger(resourceId.id);
        }
    }

    @NotNull
    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getIntArray(resourceId.id);
        } else {
            return mSuperResources.getIntArray(resourceId.id);
        }
    }


    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        final ResourceId resourceId = getNewId(id);

        if (resourceId.isNew) {
            return super.getBoolean(resourceId.id);
        } else {
            return mSuperResources.getBoolean(resourceId.id);
        }
    }

    private String format(String raw, Object... formatArgs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return String.format(getConfiguration().getLocales().get(0), raw, formatArgs);
        } else {
            return String.format(Locale.getDefault(), raw, formatArgs);
        }
    }


    private ResourceId getNewId(int id) {
        try {
            String typeName = mSuperResources.getResourceTypeName(id);
            String entryName = mSuperResources.getResourceEntryName(id);

            int newId = super.getIdentifier(entryName, typeName, mPackageInfo.packageName);
            if (newId == 0) {
                return new ResourceId(id, false);
            } else {
                return new ResourceId(newId, true);
            }
        } catch (Throwable e) {
            return new ResourceId(id, false);
        }
    }


    private final static class ResourceId {
        private int id;
        private boolean isNew;

        private ResourceId(int id, boolean isNew) {
            this.id = id;
            this.isNew = isNew;
        }
    }
}
