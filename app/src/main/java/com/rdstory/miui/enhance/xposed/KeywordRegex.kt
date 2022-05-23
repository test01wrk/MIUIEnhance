package com.rdstory.miui.enhance.xposed

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.util.SparseArray
import android.widget.EditText
import com.rdstory.miui.enhance.xposed.Hook.Companion.SECURITY_CENTER_PROCESS_REMOTE
import com.rdstory.miui.enhance.xposed.Hook.Companion.SECURITY_CENTER_PROCESS_UI
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.regex.PatternSyntaxException

@SuppressLint("LongLogTag")
object KeywordRegex : IHook {
    private const val TAG = "${Hook.TAG_PREFIX}.KeywordRegex"

    override fun initHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        // hook keyword judge method
        when (lpparam.processName) {
            SECURITY_CENTER_PROCESS_REMOTE -> {
                XposedHelpers.findAndHookMethod(
                    "com.miui.antispam.policy.b.b", lpparam.classLoader,
                    "d", String::class.java, Int::class.java, Int::class.java,
                    judgeMethod
                )
            }
            SECURITY_CENTER_PROCESS_UI -> {
                XposedHelpers.findAndHookMethod("com.miui.antispam.ui.activity.KeywordListActivity\$b", lpparam.classLoader,
                    "onClick", DialogInterface::class.java, Int::class.java,
                    keywordEditClickMethod
                )
            }
            else -> return
        }
        XposedBridge.log("[${TAG}] process hooked: ${lpparam.processName}")
    }

    private val keywordEditClickMethod = object : XC_MethodHook() {
        private var unHook: Unhook? = null
        override fun beforeHookedMethod(param: MethodHookParam) {
            val keyword = (XposedHelpers.getObjectField(param.thisObject, "a") as EditText)
                .text.toString().trim()
            unHook = XposedHelpers.findAndHookMethod(
                String::class.java, "contains",
                CharSequence::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.thisObject == keyword && (param.args[0] == "," || param.args[0] == "，")) {
                            param.result = false // skip check comma
                        }
                    }
                })
        }
        override fun afterHookedMethod(param: MethodHookParam) {
            unHook?.unhook()
            unHook = null
        }
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