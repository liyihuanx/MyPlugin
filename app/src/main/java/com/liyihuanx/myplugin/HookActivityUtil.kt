package com.liyihuanx.myplugin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import java.lang.Exception
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author liyihuan
 * @date 2022/02/23
 * @Description
 */
object HookActivityUtil {

    const val target_intent = "target_intent"

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
                // 遍历参数
                var index = 0
                for (i in args.indices) {
                    // 取出intent参数的下标
                    Log.d("QWER", "hookAMS: $i")
                    if (args[i] is Intent) {
                        index = i
                        break
                    }
                }

                // 替换成ProxyIntent
                val proxyIntent = Intent()
                proxyIntent.component = ComponentName(
                    "com.liyihuanx.myplugin",
                    "com.liyihuanx.myplugin.ProxyActivity"
                )
                // 把原本的Intent做保存
                proxyIntent.putExtra(target_intent, args[index] as Intent)

                args[index] = proxyIntent

            }
            // 继续执行原有的启动流程
            method.invoke(mInstance, *(args ?: arrayOfNulls<Any>(0)))
        }

        // 将系统的Instance替换成我们的代理的
        mInstanceField.set(iActivityTaskManagerSingleton, proxyInstance)


    }


    // 从ASM检验完回来，继续启动Activity的流程
    // ActivityStackSupervisor#realStartActivityLocked
    //      -> mService.getLifecycleManager().scheduleTransaction(clientTransaction)
    //      -> ClientLifecycleManager.scheduleTransaction
    //      -> ClientTransaction.schedule()
    //      -> mClient.scheduleTransaction(this) --> IApplicationThread

    // 回到ActivityThread中的ApplicationThread
    // -> ActivityThread.this.scheduleTransaction(transaction)
    // -> ClientTransactionHandler.scheduleTransaction
    // -> sendMessage(ActivityThread.H.EXECUTE_TRANSACTION, transaction) --> 发送msg
    // -> mH.handleMessage() --> 接收消息
    // -> transaction == ClientTransaction == msg.obj --> mActivityCallbacks
    //
    // 拿到intent修改成 MainActivity
    // LaunchActivityItem(Intent) --> Intent --> ClientTransaction#mActivityCallbacks

    fun hookHandle() {
        // 1.拿到Handler对象 --> 存在ActivityThread --> sCurrentActivityThread
        val activityThreadClazz = Class.forName("android.app.ActivityThread")
        val sCurrentActivityThreadField =
            activityThreadClazz.getDeclaredField("sCurrentActivityThread")
        sCurrentActivityThreadField.isAccessible = true
        val activityThread = sCurrentActivityThreadField.get(null)

        val handlerField = activityThreadClazz.getDeclaredField("mH")
        handlerField.isAccessible = true
        val handler = handlerField.get(activityThread)

        // 2.拿到Handler的mCallback
        val handlerClazz = Class.forName("android.os.Handler")
        val mCallbackField = handlerClazz.getDeclaredField("mCallback")
        mCallbackField.isAccessible = true

        // 3.给mCallback赋值,为什么这么做，看dispatchMessage
        val pluginCallback = object : Handler.Callback {
            override fun handleMessage(msg: Message): Boolean {
                // 4.怎么获得intent并替换
                when (msg.what) {
                    // android-28以上，加入状态管理的
                    159 -> {
                        val mActivityCallbacksField =
                            msg.obj.javaClass.getDeclaredField("mActivityCallbacks")
                        mActivityCallbacksField.isAccessible = true
                        val mActivityCallbacks = mActivityCallbacksField.get(msg.obj) as List<*>
                        for (i in mActivityCallbacks.indices) {
                            if (mActivityCallbacks[i]!!.javaClass.simpleName == "LaunchActivityItem") {
                                val launchActivityItem = mActivityCallbacks[i]
                                // 获取启动代理的 Intent
                                val mIntentField = launchActivityItem!!.javaClass
                                    .getDeclaredField("mIntent")
                                mIntentField.isAccessible = true
                                val proxyIntent = mIntentField.get(launchActivityItem) as Intent
                                // 目标 intent 替换 proxyIntent
                                val intent = proxyIntent.getParcelableExtra<Intent>(target_intent)
                                if (intent != null) {
                                    mIntentField.set(launchActivityItem, intent)
                                }
                                // 要不要加这个return ？
//                                return false
                            }
                        }
                    }

                    // 28 一下
                    100 -> {
                        val intentField = msg.obj.javaClass.getDeclaredField("intent")
                        intentField.isAccessible = true
                        // 启动代理Intent
                        val proxyIntent = intentField[msg.obj] as Intent
                        // 启动插件的 Intent
                        val intent =
                            proxyIntent.getParcelableExtra<Intent>(target_intent)
                        if (intent != null) {
                            intentField[msg.obj] = intent
                        }
                    }
                }


                // 这里一定return false
                return false
            }

        }

        mCallbackField.set(handler, pluginCallback)

    }
}