package com.rdstory.miui.enhance.xposed

import android.os.Bundle
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Method

class ClassHelper(private val classLoader: ClassLoader) {
    fun findDialogMethod(clazz: Class<*>): Array<out Method>? {
        return XposedHelpers.findMethodsByExactParameters(
            clazz,
            Void.TYPE,
            XposedHelpers.findClass("miuix.appcompat.app.AlertDialog\$Builder", classLoader)
        )
    }

    fun findFirstFieldByValue(obj: Any?, value: Any, limitNames: List<String>? = null): Field? {
        return findFirstField(obj) { field ->
            val oldAccessible = field.isAccessible
            field.isAccessible = true
            val found = field.get(obj) == value && limitNames?.contains(field.name) != false
            field.isAccessible = oldAccessible
            return@findFirstField found
        }
    }
    fun findFirstFieldByType(obj: Any?, type: Class<*>, limitNames: List<String>? = null): Field? {
        return findFirstField(obj) { field ->
            type.isAssignableFrom(field.type) && limitNames?.contains(field.name) != false
        }
    }

    private fun findFirstField(obj: Any?, predicate: (field: Field) -> Boolean): Field? {
        var clz = obj?.javaClass
        while (clz != null) {
            clz.declaredFields.find(predicate)?.let {
                it.isAccessible = true
                return it
            }
            clz = clz.superclass
        }
        return null
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