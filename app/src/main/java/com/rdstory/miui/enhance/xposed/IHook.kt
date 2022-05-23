package com.rdstory.miui.enhance.xposed

import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IHook {
    fun initHook(lpparam: XC_LoadPackage.LoadPackageParam)
}