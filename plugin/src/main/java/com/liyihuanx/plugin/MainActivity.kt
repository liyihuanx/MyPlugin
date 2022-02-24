package com.liyihuanx.plugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("QWER", "启动插件的Activity")
        setContentView(R.layout.activity_main)
    }
}