package cn.fxlcy.simpleskin;

public class SkinInfo {
    private String skinName;
    private String path;

    public SkinInfo(String path){
        this(path,path);
    }


    public SkinInfo(String skinName,String path){
        this.skinName = skinName;
        this.path = path;
    }

    public String getSkinName() {
        return skinName;
    }

    public String getPath() {
        return path;
    }


}
