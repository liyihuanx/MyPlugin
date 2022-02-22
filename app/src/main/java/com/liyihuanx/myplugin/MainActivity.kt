package com.liyihuanx.myplugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvCenter.setOnClickListener {
//            loadPluginClass2()

            ErrorTest.pluginFun()
        }


    }

    /**
     * 加载插件方法类
     */
    private fun loadPluginClass(){
        val pathClassloader = PathClassLoader("/data/data/com.liyihuanx.myplugin/fix1.dex", classLoader)
        val clazz = pathClassloader.loadClass("com.liyihuanx.plugin.ErrorTest")
        val method = clazz.getMethod("pluginFun")
        method.invoke(null)
    }

    private fun loadPluginClass2(){
        val clazz = Class.forName("com.liyihuanx.plugin.ErrorTest")
        val method = clazz.getMethod("pluginFun")
        method.invoke(null)
    }
}