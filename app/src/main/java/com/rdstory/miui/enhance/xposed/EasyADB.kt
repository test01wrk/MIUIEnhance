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
        XposedHelpers.findAndHookConstructor(
            "com.miui.permcenter.install.AdbInputApplyActivity",
            lpparam.classLoader,
            object : XC_MethodHook() {
                private var hooked = false
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hooked) return
                    hooked = true
                    val countVarList = arrayOf("e", "_w")
                    val countVar = countVarList.find { v ->
                        XposedHelpers.getIntField(param.thisObject, v) == 5
                    } ?: return
                    XposedHelpers.setIntField(param.thisObject, countVar, 1)
                    XposedHelpers.findAndHookMethod(
                        param.thisObject::class.java,
                        "onClick", View::class.java,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                XposedHelpers.setIntField(param.thisObject, countVar, 1)
                            }
                        }
                    )
                }
            })

        XposedHelpers.findAndHookConstructor(
            "com.miui.permcenter.install.AdbInstallActivity",
            lpparam.classLoader,
            object : XC_MethodHook() {
                private var hooked = false
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hooked) return
                    hooked = true
                    try {
                        XposedHelpers.findAndHookMethod(
                            param.thisObject::class.java,
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
                    } catch (ignore: Throwable) {
                    }
                    try {
                        XposedHelpers.findAndHookMethod(
                            param.thisObject::class.java,
                            "Je",
                            String::class.java,
                            object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    param.result = true
                                    Toast.makeText(param.thisObject as Context, "USB install allowed", Toast.LENGTH_LONG).show()
                                    XposedBridge.log("[$TAG] adb USB install allowed")
                                }
                            })
                    } catch (ignore: Throwable) {
                    }
                }
            }
        )
        XposedHelpers.findAndHookConstructor(
            "com.miui.permcenter.install.AdbInstallVerifyActivity",
            lpparam.classLoader,
            object : XC_MethodHook() {
                private var hooked = false
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hooked) return
                    hooked = true

                    XposedHelpers.findAndHookMethod(
                        param.thisObject::class.java,
                        "onCreate",
                        Bundle::class.java,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                XposedHelpers.callMethod(param.thisObject, "yd")
                                (param.thisObject as Activity).finish()
                            }
                        })
                }
            })


        XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
    }
}