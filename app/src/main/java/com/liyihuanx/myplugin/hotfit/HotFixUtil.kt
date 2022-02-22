package com.liyihuanx.myplugin.hotfit

import android.content.Context
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.ArrayList

/**
 * @author liyihuan
 * @date 2022/02/22
 * @Description 简单版热修复
 */
object HotFixUtil {


    fun hotfixUp(context: Context, path: String) {
        // 不存在结束
        val fixFile = File(path)
        if (!fixFile.exists()) {
            return
        }

        val patchs: MutableList<File> = ArrayList()
        patchs.add(fixFile)

        // 拿到app的classloader
        val hostPathClassLoader = context.classLoader

        // 拿到hostPathClassLoader中的pathList变量
        val pathListField: Field = ReflectUtil.findField(hostPathClassLoader, "pathList")
        val pathList = pathListField[hostPathClassLoader]

        // 拿到pathList的dexElements
        val dexElementsField: Field = ReflectUtil.findField(pathList, "dexElements")

        // 拿到原本存放dex文件的Elements
        val oldElements = dexElementsField[pathList] as Array<*>

        // 找到对应构建dex文件的方法
        val makePathElements: Method = ReflectUtil.findMethod(pathList, "makePathElements",
            MutableList::class.java,
            File::class.java,
            MutableList::class.java)
        val ioExceptions = ArrayList<IOException>()
        val fixElements = makePathElements.invoke(pathList,
            patchs,
            context.cacheDir,
            ioExceptions) as Array<Any?>


        // 合并patchElement+oldElement = newElement （Array.newInstance）
        // 创建一个新数组，大小 oldElements+patchElements
        // int[].class.getComponentType() ==int.class
        val newElements = java.lang.reflect.Array.newInstance(oldElements.javaClass.componentType,
            oldElements.size + fixElements.size) as Array<Any>

        System.arraycopy(fixElements, 0, newElements, 0, fixElements.size)
        System.arraycopy(oldElements, 0, newElements, fixElements.size, oldElements.size)
        // 反射把oldElement赋值成newElement
        dexElementsField[pathList] = newElements


    }




}