package com.liyihuanx.plugin

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

/**
 * @author liyihuan
 * @date 2022/02/28
 * @Description
 */
abstract class BaseActivity : AppCompatActivity() {

    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = ContextThemeWrapper(baseContext, 0)
        loadPluginResources()
        val view = LayoutInflater.from(mContext).inflate(getLayout(), null)
        setContentView(view)
    }


    abstract fun getLayout(): Int


    private fun loadPluginResources() {
        val resources: Resources? = ResourcesLoadUtils.getResources(application)

        val clazz: Class<out Context?> = mContext.javaClass
        try {
            val mResourcesField = clazz.getDeclaredField("mResources")
            mResourcesField.isAccessible = true
            mResourcesField[mContext] = resources
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}