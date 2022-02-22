package com.liyihuanx.myplugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class ProxyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxy)
        Log.d("QWER", "启动代理的Activity")

    }
}