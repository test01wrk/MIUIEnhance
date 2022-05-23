package com.rdstory.miui.enhance.xposed

import android.os.Bundle
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object EasyPerm : IHook {
    private const val TAG = "${Hook.TAG_PREFIX}.EasyPerm"

    override fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.processName != Hook.SECURITY_CENTER_PROCESS_UI) return
        XposedHelpers.findAndHookMethod("com.miui.permcenter.privacymanager.g", lpparam.classLoader,
            "onCreate", Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // skip timer
                    val handler = XposedHelpers.getObjectField(param.thisObject, "a")
                    XposedHelpers.setIntField(handler, "a", 0)
                    val bundle = XposedHelpers.callMethod(param.thisObject, "getArguments") as? Bundle
                    val perm = bundle?.getString("permName")
                    val pkgName = bundle?.getString("pkgName")
                    XposedBridge.log("[$TAG] faster permission timer. perm: $perm, pkg: $pkgName")
                }
            }
        )
        XposedHelpers.findAndHookMethod("com.miui.permcenter.privacymanager.i", lpparam.classLoader,
            "onCreate", Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // auto check
                    XposedHelpers.setBooleanField(param.thisObject, "l", true)
                }
            }
        )
        XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
    }
}