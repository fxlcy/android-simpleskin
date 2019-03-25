package cn.fxlcy.fix;

import android.content.Context;
import android.widget.Toast;

public class TestFix {
    public static void toast(Context context) {
        Toast.makeText(context, "hello world!", Toast.LENGTH_SHORT).show();
    }
}
