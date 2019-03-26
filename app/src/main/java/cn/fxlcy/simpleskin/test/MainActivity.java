package cn.fxlcy.simpleskin.test;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import cn.fxlcy.simpleskin.R;
import cn.fxlcy.simpleskin.SkinApplicator;
import cn.fxlcy.simpleskin.SkinManager;
import cn.fxlcy.simpleskin.SkinResources;
import cn.fxlcy.simpleskin.ViewType;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
/*
        final Context context = new ViewStubContext(this);
        final LayoutInflater inflater = LayoutInflater.from(this).cloneInContext(context);
        LayoutInflaterCompat.setFactory2(inflater, new ViewStubInflater(this));
*/

        SkinManager.SkinConfig config = SkinManager.SkinConfig.obtain(this
                , new SkinManager.OnSkinConfigInitializer() {
                    @Override
                    public SkinManager.SkinConfig init(SkinManager.SkinConfig skinConfig) {
                        skinConfig.registerSkinApplicator(new ViewType<View>(View.class, true), new SkinApplicator<View>() {
                            @Override
                            protected int[] attrIds() {
                                return new int[]{};
                            }

                            @Override
                            protected void apply(final View view, final SkinResources resources, Resources.Theme theme, int attrId, final int value) {
                                switch (attrId) {
                                    case android.R.attr.layout_width:
                                        view.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                view.getLayoutParams().width = resources.getDimensionPixelSize(value);
                                                view.requestLayout();
                                            }
                                        });
                                        break;
                                    case android.R.attr.layout_height:
                                        view.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                view.getLayoutParams().width = resources.getDimensionPixelSize(value);
                                                view.requestLayout();
                                            }
                                        });
                                        break;
                                }
                            }
                        });

                        return skinConfig;
                    }
                });

        SkinManager.getInstance().installViewFactory(this, config);
        super.onCreate(savedInstanceState);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));

        setContentView(R.layout.activity_main);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));

      /*  ZipInputStream zis = null;
        InputStream is = null;

        List<DexFile> dexFileList = new ArrayList<>();

        try {
            is = getAssets().open("fix/fix.dex");
            zis = new ZipInputStream(is);

            ZipEntry zipEntry;
            byte[] bytes = new byte[1024 * 1000 * 10];
            int readLength;

            while ((zipEntry = zis.getNextEntry()) != null) {
                final String name = zipEntry.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    ByteArrayOutputStream byteArrayOutputStream
                            = new ByteArrayOutputStream();

                    while ((readLength = zis.read(bytes, 0, bytes.length)) != -1) {
                        byteArrayOutputStream.write(bytes, 0, readLength);
                    }

                    byteArrayOutputStream.close();

                    DexFile dexFile = DexFileCompat.loadDexFile(ByteBuffer.wrap(byteArrayOutputStream
                            .toByteArray()));

                    dexFileList.add(dexFile);
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
        }


        for (DexFile dexFile : dexFileList) {
            Class clazz = dexFile.loadClass("cn.fxlcy.fix.TestFix", getClassLoader());


            Log.i(TAG,clazz.getName());
        }*/
    }


    public void onClick(View view) {
        if (view.getId() == R.id.btn_change_skin) {
            SkinManager.getInstance().switchSkinByAssets(this, "skin/skin1.skin", null);
        } else if (view.getId() == R.id.btn_restore_skin) {
            SkinManager.getInstance().restoreSkin(this);
        }
    }
}
