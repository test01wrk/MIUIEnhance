package com.rdstory.miui.enhance.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Hook : IXposedHookLoadPackage {
    companion object {
        const val TAG_PREFIX = "MIUIEnhance"
        private const val TAG = "${TAG_PREFIX}.Hook"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        // hook remote process only
        if (
            lpparam?.packageName == "com.miui.securitycenter" &&
            lpparam.processName == "com.miui.securitycenter.remote"
        ) {
            try {
                KeywordRegex.initHook(lpparam)
                XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
            } catch (e: Exception) {
                XposedBridge.log("[${TAG}] failed to hook: ${lpparam.processName}. ${e.message}")
            }
        }
    }
}