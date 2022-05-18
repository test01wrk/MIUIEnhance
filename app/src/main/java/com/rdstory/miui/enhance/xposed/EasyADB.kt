package com.rdstory.miui.enhance.xposed

import android.app.Activity
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object EasyADB {
    private const val TAG = "${Hook.TAG_PREFIX}.EasyADB"

    fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.processName != Hook.SECURITY_CENTER_PROCESS_UI) return
        val adbInputApplyActivity = XposedHelpers.findClass(
            "com.miui.permcenter.install.AdbInputApplyActivity",
            lpparam.classLoader
        )
        XposedHelpers.findAndHookConstructor(
            adbInputApplyActivity,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedHelpers.setIntField(param.thisObject, "e", 1)
                }
            })
        XposedHelpers.findAndHookMethod(
            adbInputApplyActivity,
            "onClick", View::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedHelpers.setIntField(param.thisObject, "e", 1)
                }
            }
        )
        val adbInstallActivity = XposedHelpers.findClass(
            "com.miui.permcenter.install.AdbInstallActivity",
            lpparam.classLoader
        )
        XposedHelpers.findAndHookMethod(
            adbInstallActivity,
            "z",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    XposedHelpers.setIntField(activity, "c", -1)
                    activity.finish()
                    Toast.makeText(activity, "USB install allowed", Toast.LENGTH_LONG).show()
                    XposedBridge.log("[$TAG] adb USB install allowed")
                }
            }
        )
    }
}