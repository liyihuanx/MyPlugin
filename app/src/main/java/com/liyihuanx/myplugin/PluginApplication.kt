package com.liyihuanx.myplugin

import android.app.Application

/**
 * @author liyihuan
 * @date 2022/02/21
 * @Description
 */
class PluginApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        PluginLoadUtil.loadPlugin(this)
    }
}