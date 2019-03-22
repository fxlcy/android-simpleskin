package cn.fxlcy.simpleskin;

public final class SkinInfo {
    private String skinName;
    private String path;

    public SkinInfo(String path) {
        this(path, path);
    }


    public SkinInfo(String skinName, String path) {
        this.skinName = skinName;
        this.path = path;
    }

    public String getSkinName() {
        return skinName;
    }

    public String getPath() {
        return path;
    }


    @Override
    public int hashCode() {
        return ((skinName == null ? "" : skinName) + "-" + (path == null ? "" : path)).hashCode() / 2 + 0xFF;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkinInfo) {
            return hashCode() == obj.hashCode();
        }

        return false;
    }
}
