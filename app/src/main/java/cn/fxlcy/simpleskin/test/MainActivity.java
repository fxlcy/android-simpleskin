package cn.fxlcy.simpleskin.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.fxlcy.libs.unicode_emoji.UnicodeEmoji;
import cn.fxlcy.simpleskin.R;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UnicodeEmoji.getInstance().replace(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

}
