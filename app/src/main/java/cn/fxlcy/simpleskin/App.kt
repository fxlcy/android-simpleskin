package cn.fxlcy.simpleskin

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        SkinManager.getInstance().init(this)
    }
}