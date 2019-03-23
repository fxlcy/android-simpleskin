package cn.fxlcy.simpleskin.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Files {
    private Files() {
    }


    public static void copy(InputStream in, File out) throws IOException {
        final byte[] bytes = new byte[1024 * 1000 * 10];

        FileOutputStream fos = null;
        int len;
        try {
            File parent = out.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    Log.e("Files", "folder creation failed");
                }
            }

            if (!out.createNewFile()) {
                Log.e("Files", "file creation failed");
            }
            fos = new FileOutputStream(out);

            while ((len = in.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
        } catch (Throwable ex) {
            throw new IOException(ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
