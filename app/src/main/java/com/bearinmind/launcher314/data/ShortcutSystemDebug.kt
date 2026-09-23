package com.bearinmind.launcher314.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShortcutSystemDebug {
    fun capture(context: Context): String {
        val pm = context.packageManager
        val home = runCatching { pm.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY) }.getOrNull()
        val homeName = home?.activityInfo?.let { "${it.packageName}/${it.name}" } ?: "null"
        val handlers = runCatching {
            pm.queryIntentActivities(Intent("android.content.pm.action.CONFIRM_PIN_SHORTCUT"), PackageManager.MATCH_DEFAULT_ONLY)
                .joinToString(",") { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
        }.getOrElse { "ERROR:${it.javaClass.simpleName}" }
        val sm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) runCatching { context.getSystemService(ShortcutManager::class.java) }.getOrNull() else null
        val supported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) runCatching { sm?.isRequestPinShortcutSupported }
            .fold({ it?.toString() ?: "null" }, { "ERROR:${it.javaClass.simpleName}:${it.message}" }) else "n/a"
        val main = runCatching { pm.getActivityInfo(ComponentName(context, com.bearinmind.launcher314.MainActivity::class.java), 0) }.getOrNull()
        return buildString {
            appendLine("Prime Launcher — System shortcut state")
            appendLine("Captured: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}")
            appendLine("Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("home=$homeName")
            appendLine("primeIsHome=${home?.activityInfo?.packageName == context.packageName}")
            appendLine("pinSupported=$supported")
            appendLine("confirmHandlers=[$handlers]")
            appendLine("mainEnabled=${main?.enabled}")
        }
    }
}
