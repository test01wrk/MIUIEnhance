
package com.rdstory.miui.enhance.xposed

import android.annotation.SuppressLint
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


@Suppress("UNCHECKED_CAST")
@SuppressLint("LongLogTag")
object CustomSettings : IHook {
    private const val TAG = "${Hook.TAG_PREFIX}.CustomSettings"

    override fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "com.android.camera") {
            hookCameraSettings(lpparam)
        }
        if (lpparam.packageName == "com.android.settings") {
            hookSettings(lpparam)
        }
    }

    private fun hookCameraSettings(lpparam: XC_LoadPackage.LoadPackageParam) {
        val systemPropClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
        XposedHelpers.findAndHookMethod(systemPropClass,
            "getBoolean", String::class.java, Boolean::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.args[0] == "camera_always_keep_screen_on") {
                        param.result = true;
                    }
                }
            })
    }
    private fun hookSettings(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "com.android.settings.KeyguardTimeoutListPreference",
            lpparam.classLoader,
            "disableUnusableTimeouts",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val entries = XposedHelpers.callMethod(param.thisObject, "getEntries") as Array<CharSequence>
                    val entryValues = XposedHelpers.callMethod(param.thisObject, "getEntryValues") as Array<CharSequence>
                    val maxValue = entryValues.maxOf { v -> v.toString().toLongOrNull() ?: 0 }
                    if (maxValue < 3600000L) {
                        XposedHelpers.callMethod(param.thisObject, "setEntries",
                            entries.toMutableList().apply { add("1 小时")  }.toTypedArray());
                        XposedHelpers.callMethod(param.thisObject, "setEntryValues",
                            entryValues.toMutableList().apply { add("3600000") }.toTypedArray());
                    }
                    param.result = null;
                }
            })
    }
}