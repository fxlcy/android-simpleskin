package cn.fxlcy.simpleskin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SkinManager.getInstance().installInflater(this);
        super.onCreate(savedInstanceState);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));

        setContentView(R.layout.activity_main);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));
    }


    public void onClick(View view) {
        if (view.getId() == R.id.btn_change_skin) {

            try {
                InputStream is = getAssets().open("skin/skin2.skin");
                String path = getApplicationInfo().dataDir + File.separator + "skin"
                        + File.separator + "skin1.skin";
                File file = new File(path);
                file.getParentFile().mkdirs();
                file.createNewFile();
                OutputStream os = new FileOutputStream(path);


                final byte[] buffer = new byte[1024 * 1000];

                int len;

                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }


                os.close();


                is.close();

                SkinManager.getInstance().switchSkin(this, new SkinInfo(path), null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.btn_restore_skin) {
            SkinManager.getInstance().restoreSkin(this);
        }
    }
}
