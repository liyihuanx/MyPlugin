package com.liyihuanx.myplugin

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvCenter.setOnClickListener {
            val intent = Intent(this, ProxyActivity::class.java)
            startActivity(intent)
//            loadPluginActivity()

            // 启动Activity时到进入ASM的流程，拿到intent修改成ProxyActivity
            // activity
            //   -> Instrumentation#execStartActivity
            //   -> ActivityTaskManager#IActivityTaskManagerSingleton = (AMS)
            //   -> AMS#startActivity
            //   -> ActivityTaskManagerService#startActivityAsUser
            //   -> ActivityStarter#excute 拿到result

            // Instrumentation#checkStartActivityResult

            // 从ASM检验完回来，继续启动Activity的流程
            // ActivityStackSupervisor#realStartActivityLocked ->realStartActivityLocked()
            //      -> mService.getLifecycleManager().scheduleTransaction(clientTransaction)
            //      -> ClientLifecycleManager.scheduleTransaction
            //      -> ClientTransaction.schedule()
            //      -> mClient.scheduleTransaction(this) --> IApplicationThread

            // 回到ActivityThread中的ApplicationThread
            // -> ActivityThread.this.scheduleTransaction(transaction)
            // -> ClientTransactionHandler.scheduleTransaction
            // -> sendMessage(ActivityThread.H.EXECUTE_TRANSACTION, transaction);
            // -> mH.handleMessage()
            // 拿到intent修改成 MainActivity

        }
    }

    /**
     * 加载插件方法类
     */
    private fun loadPluginClass() {
        val pathClassloader =
            PathClassLoader("/data/data/com.liyihuanx.myplugin/plugin-debug.apk", classLoader)
        val clazz = pathClassloader.loadClass("com.liyihuanx.plugin.ErrorTest")
        val method = clazz.getMethod("pluginFun")
        method.invoke(null)
    }

    private fun loadPluginClass2() {
        val clazz = Class.forName("com.liyihuanx.plugin.ErrorTest")
        val method = clazz.getMethod("pluginFun")
        method.invoke(null)
    }

    private fun loadPluginActivity(){
        val intent = Intent()
        intent.component = ComponentName(
            "com.liyihuanx.plugin",
            "com.liyihuanx.plugin.MainActivity"
        )
        startActivity(intent)
    }
}