package dalvik.system;

import android.content.Context;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cn.fxlcy.lib.util.PCompat;

public class DexFileCompat {
    private static Constructor sConstructor;
    private static Constructor sPathConstructor;
    private static boolean sHavePermission = true;


    public final static int VERSION_O = 26;

    public static ClassLoader getClassLoaderByApk(Context context, File apk, ClassLoader parent) {
        final boolean loadByByteBuffer = Build.VERSION.SDK_INT >= VERSION_O;

        File path = null;

        if (!loadByByteBuffer) {
            path = new File(context.getApplicationInfo().dataDir + File.separator + "skin" + File.separator + "fix" + File.separator + apk.getName() + ".dex");

            if (path.exists()) {
                try {
                    return new PathClassLoader(path.getAbsolutePath(), parent);
                } catch (Throwable ignored) {
                }
            }
        }


        ZipInputStream zis = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream fos = null;

        try {
            zis = new ZipInputStream(new FileInputStream(apk));

            ZipEntry zipEntry;

            byte[] bytes = new byte[1024 * 1000 * 10];
            int readLength;

            while ((zipEntry = zis.getNextEntry()) != null) {
                final String name = zipEntry.getName();
                if ("classes.dex".equals(name)) {
                    if (loadByByteBuffer) {

                        byteArrayOutputStream
                                = new ByteArrayOutputStream();

                        while ((readLength = zis.read(bytes, 0, bytes.length)) != -1) {
                            byteArrayOutputStream.write(bytes, 0, readLength);
                        }


                        return new InMemoryDexClassLoader(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()), parent);
                    } else {
                        File p = path.getParentFile();
                        if (!p.exists() || !p.isDirectory()) {
                            if (!p.isDirectory()) {
                                p.delete();
                            }

                            p.mkdirs();
                        }

                        path.createNewFile();

                        fos = new FileOutputStream(path);

                        while ((readLength = zis.read(bytes, 0, bytes.length)) != -1) {
                            fos.write(bytes, 0, readLength);
                        }

                        return new PathClassLoader(path.getAbsolutePath(), parent);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }

    private static DexFile loadDexByApkInputStream(Context context, InputStream is, String filename) {


        final boolean loadByByteBuffer = Build.VERSION.SDK_INT >= VERSION_O;

        File path = null;

        if (!loadByByteBuffer) {
            path = new File(context.getApplicationInfo().dataDir + File.separator + "skin" + File.separator + "fix" + File.separator + filename + ".dex");


            if (path.exists()) {
                try {
                    DexFile dexFile = loadDexFile(context, path.getAbsolutePath());
                    if (dexFile != null) {
                        return dexFile;
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        ZipInputStream zis = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream fos = null;


        try {
            zis = new ZipInputStream(is);

            ZipEntry zipEntry;

            byte[] bytes = new byte[1024 * 1000 * 10];
            int readLength;

            while ((zipEntry = zis.getNextEntry()) != null) {
                final String name = zipEntry.getName();
                if ("classes.dex".equals(name)) {
                    if (loadByByteBuffer) {
                        byteArrayOutputStream
                                = new ByteArrayOutputStream();

                        while ((readLength = zis.read(bytes, 0, bytes.length)) != -1) {
                            byteArrayOutputStream.write(bytes, 0, readLength);
                        }

                        return loadDexFile(context, ByteBuffer.wrap(byteArrayOutputStream
                                .toByteArray()));
                    } else {
                        File p = path.getParentFile();
                        if (!p.exists() || !p.isDirectory()) {
                            if (!p.isDirectory()) {
                                p.delete();
                            }

                            p.mkdirs();
                        }

                        path.createNewFile();

                        fos = new FileOutputStream(path);

                        while ((readLength = zis.read(bytes, 0, bytes.length)) != -1) {
                            fos.write(bytes, 0, readLength);
                        }


                        return loadDexFile(context, path.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    public static DexFile loadDexFileByApkFile(Context context, String path) {
        try {
            return loadDexByApkInputStream(context, new FileInputStream(path), new File(path).getName());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static DexFile loadDexFile(Context context, String path) {
        ClassLoader classLoader = null;

        if (Build.VERSION.SDK_INT >= PCompat.VERSION_P && context.getApplicationInfo().targetSdkVersion >= PCompat.VERSION_P) {
            classLoader = PCompat.compat(DexFileCompat.class);
        }

        DexFile dexFile = null;


        try {
            dexFile = new DexFile(path);
        } catch (IllegalAccessError error) {
            dexFile = loadDexFileByPathReflection(path);
        } catch (Throwable ignored) {
        }

        PCompat.reset(DexFileCompat.class, classLoader);

        return dexFile;
    }

    public static DexFile loadDexFile(Context context, ByteBuffer byteBuffer) {

        ClassLoader classLoader = null;

        if (Build.VERSION.SDK_INT >= PCompat.VERSION_P && context.getApplicationInfo().targetSdkVersion >= PCompat.VERSION_P) {
            classLoader = PCompat.compat(DexFileCompat.class);
        }

        DexFile dexFile;

        if (sHavePermission) {
            try {
                dexFile = new DexFile(byteBuffer);
            } catch (IllegalAccessError error) {
                synchronized (DexFileCompat.class) {
                    sHavePermission = false;
                }
                dexFile = loadDexFileByByteButterReflection(byteBuffer);
            }
        } else {
            dexFile = loadDexFileByByteButterReflection(byteBuffer);
        }

        if (classLoader != null) {
            PCompat.reset(DexFileCompat.class, classLoader);
        }

        return dexFile;
    }

    private static DexFile loadDexFileByByteButterReflection(ByteBuffer byteBuffer) {
        if (sConstructor == null) {
            synchronized (DexFileCompat.class) {
                if (sConstructor == null) {
                    try {
                        sConstructor = DexFile.class.getDeclaredConstructor(ByteBuffer.class);
                        sConstructor.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (sConstructor != null) {
            try {
                return (DexFile) sConstructor.newInstance(byteBuffer);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static DexFile loadDexFileByPathReflection(String path) {
        if (sPathConstructor == null) {
            synchronized (DexFileCompat.class) {
                if (sPathConstructor == null) {
                    try {
                        sPathConstructor = DexFile.class.getDeclaredConstructor(String.class);
                        sPathConstructor.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (sPathConstructor != null) {
            try {
                return (DexFile) sPathConstructor.newInstance(path);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
