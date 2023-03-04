package com.rdstory.miui.enhance.xposed

import android.os.Bundle
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

class ClassHelper(private val classLoader: ClassLoader) {
    fun findDialogMethod(clazz: Class<*>): Array<out Method>? {
        return XposedHelpers.findMethodsByExactParameters(
            clazz,
            Void.TYPE,
            XposedHelpers.findClass("miuix.appcompat.app.AlertDialog\$Builder", classLoader)
        )
    }

    fun findFieldNameMatchValue(obj: Any, value: Any, optionNames: List<String> = ( 'a'..'z').map { it.toString() }): String? {
        return optionNames.find {
            val field = XposedHelpers.findFieldIfExists(obj::class.java, it)
            return@find field?.get(obj) == value
        }
    }

    fun findOnCreateMethod(activityClass: Class<*>): Method? {
        var clazz: Class<*>? = activityClass
        while (clazz != null) {
            XposedHelpers.findMethodExactIfExists(
                clazz, "onCreate", Bundle::class.java)?.let { return it }
            clazz = clazz.superclass
        }
        return null
    }
}