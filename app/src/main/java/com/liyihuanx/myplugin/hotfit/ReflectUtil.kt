package com.liyihuanx.myplugin.hotfit

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

/**
 * @author liyihuan
 * @date 2022/02/22
 * @Description
 */
object ReflectUtil {


    /**
     * 从传入的类到其父类中寻找Field
     */
    @JvmStatic
    fun findField(instance: Any, name: String): Field {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                //查找当前类的 属性(不包括父类)
                val field = clazz.getDeclaredField(name)
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                return field
            } catch (e: NoSuchFieldException) {
                // ignore and search next
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("Field " + name + " not found in " + instance.javaClass)
    }


    /**
     * 寻找method方法
     */
    @JvmStatic
    fun findMethod(instance: Any, name: String, vararg parameterTypes: Class<*>?): Method {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val method = clazz.getDeclaredMethod(name, *parameterTypes)
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {
                // ignore and search next
            }
            clazz = clazz.superclass
        }
        throw NoSuchMethodException("Method "
                + name
                + " with parameters "
                + Arrays.asList(*parameterTypes)
                + " not found in " + instance.javaClass)
    }


}