package com.liyihuanx.myplugin

import android.content.Context
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader

/**
 * @author liyihuan
 * @date 2022/02/21
 * @Description 插件化加载类
 */
object PluginLoadUtil {

    private const val plugin_path = "/data/data/com.liyihuanx.myplugin/plugin-debug.apk"

    /**
     * 1.dex文件是什么，代表什么，存放在哪
     * 2.classload是什么，几种classload的区别
     *
     * 追踪一下传入一个dex文件路径，最终会被谁拿到使用
     * PathClassLoad --> BaseClassLoad#pathList --> DexPathList --> dexElements数组存放dex文件
     *
     * this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory,suppressedExceptions, definingContext, isTrusted);
     *   makeDexElements --> 生成一个dex/resource path元素数组
     *   splitDexPath --> 使用路径将给定的路径字符串拆分为文件元素
     * 走完这一步，传入的dex路径就保存在 DexPathList#dexElements中, 也就是BaseClassLoad的pathList
     *
     * 根据双亲委派机制，加载类时最终会跑到ClassLoad.java#loadclass(String className),根据类名加载
     * 而load之前 会调用 findClassLoad(),找到了会返回，找不到会加载，就相当于做缓存，所以一个类被只会加载一次
     * 这就导致了， 源文件dex 和 fix文件dex，要是有同类名的class。会加载dex文件在前的类 ---> 热修复
     *
     * 所以，想要修改的话，就将你的dex文件，插入到dexElements的第一个，这样按顺序加载第一个就是你的dex文件
     *
     */
    fun loadPlugin(context: Context) {
        // 1.拿到加载项目的 classloader
        val baseDexClassLoaderClazz = Class.forName("dalvik.system.BaseDexClassLoader")
        val pathListFile = baseDexClassLoaderClazz.getDeclaredField("pathList")
        pathListFile.isAccessible = true

        // 2.拿到DexPathList
        val dexPathListClazz = Class.forName("dalvik.system.DexPathList")
        val dexElementsFile = dexPathListClazz.getDeclaredField("dexElements")
        dexElementsFile.isAccessible = true


        // 3.获取宿主对象的
        // 拿到ClassLoad
        val hostPathClassloader = context.classLoader
        // DexPathList类的对象
        val hostPathList = pathListFile.get(hostPathClassloader)
        // 拿到dexElements            = dexElementsFile[hostPathList]
        val hostDexElements = dexElementsFile.get(hostPathList) as Array<*>


        // 4.获取插件对象的
        // 插件的类加载器
        val pluginClassLoader: ClassLoader =
            DexClassLoader(plugin_path, context.cacheDir.absolutePath, null, hostPathClassloader)
        // DexPathList类的对象
        val pluginPathList = pathListFile.get(pluginClassLoader)
        // 拿到dexElements
        val pluginDexElements = dexElementsFile.get(pluginPathList) as Array<*>


        // 5.做合并
        // 创建一个数组,为什么不能直接创建，而要用反射
        val newDexElements =
            java.lang.reflect.Array.newInstance(hostDexElements.javaClass.componentType,
                hostDexElements.size + pluginDexElements.size) as Array<*>

        // 先加host的，再加插件的
        System.arraycopy(hostDexElements, 0, newDexElements,
            0, hostDexElements.size)

        System.arraycopy(pluginDexElements, 0, newDexElements,
            hostDexElements.size, pluginDexElements.size)


        // 6.赋值

        // 赋值
        // hostDexElements + newDexElements
//        dexElementsFile[hostPathList] = newDexElements
        dexElementsFile.set(hostPathList, newDexElements)
    }

}