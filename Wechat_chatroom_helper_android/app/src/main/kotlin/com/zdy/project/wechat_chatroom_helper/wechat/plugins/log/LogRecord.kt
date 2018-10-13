package com.zdy.project.wechat_chatroom_helper.wechat.plugins.log

import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters

object LogRecord {

    fun executeHook() {

        val logcatClass = XposedHelpers.findClass(WXObject.Tool.C.Logcat, RuntimeInfo.classloader)
        val logcatLogMethods = findMethodsByExactParameters(logcatClass, null, String::class.java, String::class.java, Array<Any>::class.java)


        logcatLogMethods.forEach { method ->
            //            val parameterTypes = method.parameterTypes.toMutableList().also { list ->
//                if (list.size == 3) {
//                    list.removeAt(2)
//                    list.add(Array<Any>::class.java)
//                }
//            }

            findAndHookMethod(logcatClass, method.name, String::class.java, String::class.java, Array<Any>::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {

                    if (!AppSaveInfo.openLogInfo()) return

                    try {

                        val str1 = param.args[0] as String
                        val str2 = param.args[1] as String

                        if (param.args[2] == null) {

                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $str2")

                        } else {
                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, *objArr)

                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $format")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }


    }


}