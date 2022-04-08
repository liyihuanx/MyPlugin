package com.liyihuanx.plugin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("QWER", "启动插件的Activity")
        tvCenter.setOnClickListener {
            val intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun onResume() {
        super.onResume()
        Log.d("QWER", "插件的onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("QWER", "插件的onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.d("QWER", "插件的onStop")
    }
}