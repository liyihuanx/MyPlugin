package com.liyihuanx.myplugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvCenter.setOnClickListener {
//            ErrorTest.pluginFun()

            val pathClassloader = PathClassLoader("/data/data/com.liyihuanx.myplugin/fix.dex",null)
            val clazz = pathClassloader.loadClass("com.liyihuanx.plugin.ErrorTest")
            val method = clazz.getMethod("pluginFun")
            method.invoke(null)

        }


        // 宿主dexElements = 宿主dexElements + 插件dexElements
//            Object[] obj = new Object[]; // 不行


    }
}