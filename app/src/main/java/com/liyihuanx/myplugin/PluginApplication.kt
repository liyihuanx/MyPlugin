package com.liyihuanx.myplugin

import android.app.Application
import com.liyihuanx.myplugin.hotfit.HotFixUtil

/**
 * @author liyihuan
 * @date 2022/02/21
 * @Description
 */
class PluginApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        HotFixUtil.hotfixUp(this, "data/data/com.liyihuanx.myplugin/fix.dex")
        PluginLoadUtil.loadPlugin(this)
    }
}