package cn.fxlcy.simpleskin.util;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
    private static final String TAG = Md5.class.getSimpleName();

    private Md5() {
    }

    public static String getMd5(String text) {
        StringBuilder hashtext = new StringBuilder();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            hashtext = new StringBuilder(bigInt.toString(16));

            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
        } catch (NoSuchAlgorithmException var5) {
            Log.e(TAG, "md5" + var5.getMessage());
        }

        return hashtext.toString();
    }
}
