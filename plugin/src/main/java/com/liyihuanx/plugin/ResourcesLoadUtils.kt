package com.liyihuanx.plugin

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import java.lang.Exception

/**
 * @author liyihuan
 * @date 2022/02/28
 * @Description
 */
object ResourcesLoadUtils {


    private const val plugin_path = "/data/data/com.liyihuanx.myplugin/plugin-debug.apk"

    private var mResources: Resources? = null

    fun getResources(context: Context): Resources? {
        if (mResources == null) {
            mResources = loadResource(context)
        }
        return mResources
    }

    private fun loadResource(context: Context): Resources? {
        // assets.addAssetPath(key.mResDir)
        try {
            val assetManager = AssetManager::class.java.newInstance()
            // 让 这个 AssetManager对象 加载的 资源为插件的
            val addAssetPathMethod =
                AssetManager::class.java.getMethod("addAssetPath", String::class.java)
            addAssetPathMethod.invoke(assetManager, plugin_path)

            // 如果传入的是Activity的 context 会不断循环，导致崩溃
            val resources = context.resources

            // 加载插件的资源的 resources
            return Resources(assetManager, resources.displayMetrics, resources.configuration)
        } catch (e: Exception) {
            e.printStackTrace()
        }



        return null
    }
    
    // resource是和context有关系的。并且在app启动时，资源文件就应该加载好了
    // Application#handleBindApplication --> appContext = ContextImpl.createAppContext 创建context
    // ContextImpl#createAppContext -->  context.setResources(packageInfo.getResources());

    // Application#handleBindApplication --> makeApplication --> ContextImpl.createAppContext


}