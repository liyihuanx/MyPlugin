package com.liyihuanx.myplugin

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author liyihuan
 * @date 2022/02/23
 * @Description
 */
object HookAMSUtil {

    //

    // 启动Activity时到进入ASM的流程，拿到intent修改成ProxyActivity
    // activity
    //   -> Instrumentation#execStartActivity
    //   -> ActivityTaskManager#IActivityTaskManagerSingleton -> IActivityTaskManagerSingleton.get() = singleton类中的Instance = (AMS)
    //   -> AMS#startActivity
    // 代理AMS的startActivity方法，替换成ProxyActivity
    fun hookAMS() {

        // 获取IActivityTaskManagerSingleton
        val activityTaskManagerClazz = Class.forName("android.app.ActivityTaskManager")
        val iActivityTaskManagerSingletonField =
            activityTaskManagerClazz.getDeclaredField("IActivityTaskManagerSingleton")
        iActivityTaskManagerSingletonField.isAccessible = true

        // 获取某个类中的变量,因为是静态所以不属于哪个类就直接传null，
        // 这里拿到了IActivityTaskManagerSingleton变量 == 单例
        val iActivityTaskManagerSingleton = iActivityTaskManagerSingletonField.get(null)

        // 获取系统的Instance = IActivityTaskManager = AMS
        val singletonClazz = Class.forName("android.util.Singleton")
        val mInstanceField = singletonClazz.getDeclaredField("mInstance")
        mInstanceField.isAccessible = true

        // android-10 以上会拿不到
        var mInstance = mInstanceField.get(iActivityTaskManagerSingleton)
        // 手动调用一下
        if (mInstance == null) {
            val methodGet = singletonClazz.getDeclaredMethod("get")
            methodGet.isAccessible = true
            methodGet.invoke(iActivityTaskManagerSingleton)
            mInstance = mInstanceField.get(iActivityTaskManagerSingleton)
        }

        // 需要代理的接口类
        val iActivityTaskManager = Class.forName("android.app.IActivityTaskManager")
        // 代理系统的Instance
        val proxyInstance = Proxy.newProxyInstance(
            Thread.currentThread().contextClassLoader,
            arrayOf<Class<*>>(iActivityTaskManager)
        )
        { proxy, method, args ->
            // 在这里对intent做处理，修改成ProxyActivity
            if ("startActivity" == method.name) {
                Log.d("QWER", "hookAMS: startActivity")
            }
            method.invoke(mInstance, *(args ?: arrayOfNulls<Any>(0)))
        }

        // 将系统的Instance替换成我们的代理的
        mInstanceField.set(iActivityTaskManagerSingleton, proxyInstance)


    }


}