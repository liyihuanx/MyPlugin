package com.liyihuanx.myplugin

import android.app.Application
import android.util.Log
import com.liyihuanx.myplugin.hotfit.HotFixUtil

/**
 * @author liyihuan
 * @date 2022/02/21
 * @Description
 */
class PluginApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PluginLoadUtil.loadPlugin(this)
        HookAMSUtil.hookAMS()
    }
}