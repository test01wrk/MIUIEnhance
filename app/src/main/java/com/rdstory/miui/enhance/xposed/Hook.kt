package com.rdstory.miui.enhance.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Hook : IXposedHookLoadPackage {
    companion object {
        const val TAG_PREFIX = "MIUIEnhance"
        private const val TAG = "${TAG_PREFIX}.Hook"
        const val SECURITY_CENTER_PKG = "com.miui.securitycenter"
        const val SECURITY_CENTER_PROCESS_UI = SECURITY_CENTER_PKG
        const val SECURITY_CENTER_PROCESS_REMOTE = "$SECURITY_CENTER_PKG.remote"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == SECURITY_CENTER_PKG) {
            arrayOf(
                KeywordRegex,
                EasyADB,
                EasyPerm
            ).forEach {
                try {
                    it.initHook(lpparam)
                } catch (e: Throwable) {
                    XposedBridge.log("[${TAG}] failed to hook: ${lpparam.processName}. ${e.message}")
                }
            }
        }
    }
}