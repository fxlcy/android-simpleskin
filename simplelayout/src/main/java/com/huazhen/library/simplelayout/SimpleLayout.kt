package com.huazhen.library.simplelayout

import android.app.Activity
import com.huazhen.library.simplelayout.inflater.CommonDrawableInflater
import com.huazhen.library.simplelayout.inflater.TextColorInflater

class SimpleLayout {
    companion object {

        @JvmStatic
        fun inject(activity: Activity, drawable: Boolean, textColor: Boolean) {
            if (drawable) {
                CommonDrawableInflater.inject(activity)
            }
            if (textColor) {
                TextColorInflater.inject(activity)
            }
        }
    }
}