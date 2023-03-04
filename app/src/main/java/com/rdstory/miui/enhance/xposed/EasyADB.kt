package com.rdstory.miui.enhance.xposed

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


object EasyADB : IHook {
    private const val TAG = "${Hook.TAG_PREFIX}.EasyADB"

    override fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.processName != Hook.SECURITY_CENTER_PROCESS_UI) return
        val classHelper = ClassHelper(lpparam.classLoader)
        val resetTimerHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val options = arrayListOf("_w").apply {
                    addAll(('a'..'z').map { it.toString() })
                }
                val countVar = classHelper.findFieldNameMatchValue(
                    param.thisObject, 5, options) ?: return
                XposedHelpers.setIntField(param.thisObject, countVar, 1)
            }
        }
        XposedHelpers.findAndHookConstructor(
            "com.miui.permcenter.install.AdbInputApplyActivity",
            lpparam.classLoader,
            resetTimerHook
        )
        XposedHelpers.findAndHookMethod(
            "com.miui.permcenter.install.AdbInputApplyActivity",
            lpparam.classLoader,
            "onClick", View::class.java,
            resetTimerHook
        )

        val adbInstallActivityClass = XposedHelpers.findClass(
            "com.miui.permcenter.install.AdbInstallActivity",
            lpparam.classLoader
        )
        try {
            classHelper.findDialogMethod(adbInstallActivityClass)?.forEach { method ->
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as Activity
                        val flagVar = classHelper
                            .findFieldNameMatchValue(param.thisObject, 0) ?: return
                        XposedHelpers.setIntField(activity, flagVar, -1)
                        activity.finish()
                        Toast.makeText(activity, "USB install allowed", Toast.LENGTH_LONG).show()
                        XposedBridge.log("[$TAG] adb USB install allowed")
                    }
                })
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            XposedHelpers.findAndHookMethod(
                adbInstallActivityClass,
                "Je",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                        Toast.makeText(param.thisObject as Context, "USB install allowed", Toast.LENGTH_LONG).show()
                        XposedBridge.log("[$TAG] adb USB install allowed")
                    }
                })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.permcenter.install.AdbInstallVerifyActivity",
                lpparam.classLoader,
                "onDestroy",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedHelpers.callMethod(param.thisObject, "C")
                    }
                })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.permcenter.install.AdbInstallVerifyActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedHelpers.callMethod(param.thisObject, "yd")
                        (param.thisObject as Activity).finish()
                    }
                })
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
    }
}