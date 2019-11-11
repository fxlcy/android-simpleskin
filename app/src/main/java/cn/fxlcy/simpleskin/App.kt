package cn.fxlcy.simpleskin

import android.app.Application
import cn.fxlcy.libs.unicode_emoji.UnicodeEmoji

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        SkinManager.getInstance().init(this)

        UnicodeEmoji.getInstance().init(this)
    }
}