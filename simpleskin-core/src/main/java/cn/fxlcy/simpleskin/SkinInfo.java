package cn.fxlcy.simpleskin;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import cn.fxlcy.simpleskin.util.Files;
import cn.fxlcy.simpleskin.util.Md5;

public final class SkinInfo {
    Schema schema;

    String path;


    private String localPath;


    private SkinInfo() {
    }

    public static SkinInfo obtainByAssets(Context context, String path) {
        SkinInfo info = new SkinInfo();

        info.schema = Schema.ASSETS;
        info.path = path;

        File file = new File(info.getLocalPath(context));
        info.localPath = file.getAbsolutePath();

        if (file.exists()) {
            return info;
        } else {
            try {
                Files.copy(context.getAssets().open(path), file);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return info;
        }

    }


    public static SkinInfo obtainByLocalPath(String path) {
        SkinInfo info = new SkinInfo();
        info.schema = Schema.FILES;
        info.path = path;
        info.localPath = path;

        return info;
    }


    public String getLocalPath(Context context) {
        if (localPath == null) {
            switch (schema) {
                case ASSETS:
                    localPath = context.getApplicationInfo().dataDir + File.separator + "skin" + File.separator + Md5.getMd5(schema.name + path)
                            + ".skin";
                    break;
                case FILES:
                    localPath = path;
            }
        }

        return localPath;
    }


    public String getUri() {
        return schema.name + path;
    }

    @Override
    public int hashCode() {
        return getUri().hashCode() / 2 + 0xFF;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkinInfo) {
            return hashCode() == obj.hashCode();
        }

        return false;
    }


    public enum Schema {
        ASSETS("assets://"),
        FILES("files:///");

        private String name;

        public String getName() {
            return name;
        }

        @NotNull
        public String toString() {
            return this.name;
        }

        public static Schema valueOfName(@Nullable String value) {
            if (value == null) {
                return null;
            }

            switch (value) {
                case "assets://":
                    return ASSETS;
                case "files:///":
                    return FILES;
            }

            return null;
        }

        Schema(String name) {
            this.name = name;
        }
    }
}
