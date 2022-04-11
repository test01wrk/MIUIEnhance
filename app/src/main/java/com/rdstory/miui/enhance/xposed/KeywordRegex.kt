package com.rdstory.miui.enhance.xposed

import android.annotation.SuppressLint
import android.util.SparseArray
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.regex.PatternSyntaxException

@SuppressLint("LongLogTag")
object KeywordRegex {
    private const val TAG = "${Hook.TAG_PREFIX}.KeywordRegex"

    fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        // hook keyword judge method
        XposedHelpers.findAndHookMethod(
            "com.miui.antispam.policy.b.b", lpparam.classLoader,
            "d", String::class.java, Int::class.java, Int::class.java,
            judgeMethod
        )
    }

    private val judgeMethod = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            val content = param?.args?.getOrNull(0) as? String ?: return
            val listType = param.args.getOrNull(1) as? Int ?: return
            val simIndex = param.args.getOrNull(2) as? Int ?: return
            // get keyword sparse array data
            val allSparseArray = try {
                XposedHelpers.getObjectField(param.thisObject, "b") as? SparseArray<*>
            } catch (e: Exception) {
                XposedBridge.log("[${TAG}] failed to retrieve keyword data. ${e.message}")
                null
            } ?: return
            val typeSparseArray = allSparseArray.get(listType) as? SparseArray<*>?: return
            val keywords = typeSparseArray.get(simIndex) as? List<*> ?: return
            for (key in keywords) {
                if (key !is String) {
                    continue
                }
                // regex match keyword
                val match = try {
                    Regex(key, RegexOption.IGNORE_CASE).containsMatchIn(content)
                } catch (ignore: PatternSyntaxException) {
                    false // not a legal regex keyword, continue
                }
                // if match, early return our result
                if (match) {
                    param.result = key
                    XposedBridge.log("[${TAG}] keyword matched: $key")
                    break
                }
            }
            // if not matched by regex, will continue with original match method,
            // which is basically "content.toLowerCase().contains(keyword.toLowerCase())"
        }
    }
}