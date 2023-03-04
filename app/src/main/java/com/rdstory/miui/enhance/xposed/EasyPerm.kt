package com.rdstory.miui.enhance.xposed

import android.os.Bundle
import android.os.Handler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object EasyPerm : IHook {
    private const val TAG = "${Hook.TAG_PREFIX}.EasyPerm"

    override fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.processName != Hook.SECURITY_CENTER_PROCESS_UI) return
        try {
            XposedHelpers.findAndHookMethod("com.miui.permcenter.privacymanager.g",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // skip timer
                        val handler = XposedHelpers.getObjectField(param.thisObject, "a")
                        XposedHelpers.setIntField(handler, "a", 0)
                        val bundle =
                            XposedHelpers.callMethod(param.thisObject, "getArguments") as? Bundle
                        val perm = bundle?.getString("permName")
                        val pkgName = bundle?.getString("pkgName")
                        XposedBridge.log("[$TAG] faster permission timer. perm: $perm, pkg: $pkgName")
                    }
                }
            )
        } catch (ignore: Throwable) {
        }
        try {
            XposedHelpers.findAndHookMethod("com.miui.permcenter.privacymanager.i",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // auto check
                        XposedHelpers.setBooleanField(param.thisObject, "l", true)
                    }
                }
            )
        } catch (ignore: Throwable) {
        }
        try {
            val classHelper = ClassHelper(lpparam.classLoader)
            XposedHelpers.findAndHookMethod("com.miui.permcenter.privacymanager.InterceptBaseFragment",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val handler =
                            classHelper.findFirstFieldByType(param.thisObject, Handler::class.java)
                                ?.get(param.thisObject)
                                ?: return
                        classHelper.findFirstFieldByType(handler, Int::class.java)
                            ?.setInt(handler, 1)
                        XposedBridge.log("[$TAG][constructor] faster permission timer")
                        XposedHelpers.findMethodsByExactParameters(
                            handler::class.java,
                            Void.TYPE,
                            Int::class.java
                        )?.forEach { method ->
                            XposedBridge.hookMethod(
                                method,
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        param.args[0] = 1;
                                        XposedBridge.log("[$TAG][setter] faster permission time")
                                    }
                                }
                            )
                        }
                    }
                })
        } catch (ignore: Throwable) {
        }
        XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
    }
}