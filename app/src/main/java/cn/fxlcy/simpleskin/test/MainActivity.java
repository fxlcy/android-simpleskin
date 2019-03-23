package cn.fxlcy.simpleskin.test;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.huazhen.library.simplelayout.inflater.BaseViewInflater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.fxlcy.simpleskin.R;
import cn.fxlcy.simpleskin.SkinInfo;
import cn.fxlcy.simpleskin.SkinManager;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";


    private static class ViewStubInflater extends BaseViewInflater {
        public ViewStubInflater(Activity activity) {
            super(activity);
        }

        @Override
        public View createView(View parent, String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            return super.createView(parent, name, context, attrs);
        }
    }


    private static class ViewStubContext extends ContextWrapper {
        public ViewStubContext(Context base) {
            super(base);
        }
    }

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
                        return skinConfig;
                    }
                });

        SkinManager.getInstance().installViewFactory(this, config);
        super.onCreate(savedInstanceState);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));

        setContentView(R.layout.activity_main);

        Log.i(TAG, String.valueOf(System.currentTimeMillis()));



    }


    public void onClick(View view) {
        if (view.getId() == R.id.btn_change_skin) {
            SkinManager.getInstance().switchSkinByAssets(this,"skin/skin2.skin",null);
        } else if (view.getId() == R.id.btn_restore_skin) {
            SkinManager.getInstance().restoreSkin(this);
        }
    }
}
