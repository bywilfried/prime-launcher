package com.bearinmind.launcher314.data

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Temporary on-device trace for external Add-to-Home debugging. */
object ShortcutDebugLog {
    private const val FILE = "shortcut_debug.log"
    private const val MAX_BYTES = 128 * 1024

    @Synchronized
    fun log(context: Context, event: String) {
        runCatching {
            val file = File(context.filesDir, FILE)
            if (file.exists() && file.length() > MAX_BYTES) {
                val tail = file.readText().takeLast(MAX_BYTES / 2)
                file.writeText("--- log truncated ---\n$tail")
            }
            val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            file.appendText("$stamp | $event\n")
        }
    }

    fun captureSystemState(context: Context) {
        val pm = context.packageManager
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
        val home = runCatching { pm.resolveActivity(homeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY) }.getOrNull()
        val homeName = home?.activityInfo?.let { "${it.packageName}/${it.name}" } ?: "null"

        val confirmIntent = android.content.Intent("android.content.pm.action.CONFIRM_PIN_SHORTCUT")
        val handlers = runCatching {
            pm.queryIntentActivities(confirmIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                .joinToString(",") { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
        }.getOrElse { "ERROR:${it.javaClass.simpleName}" }

        val shortcutManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { context.getSystemService(android.content.pm.ShortcutManager::class.java) }.getOrNull()
        } else null
        val pinSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { shortcutManager?.isRequestPinShortcutSupported }.fold(
                onSuccess = { it?.toString() ?: "null" },
                onFailure = { "ERROR:${it.javaClass.simpleName}:${it.message}" }
            )
        } else "n/a"

        val myActivity = runCatching {
            pm.getActivityInfo(
                android.content.ComponentName(context, com.bearinmind.launcher314.MainActivity::class.java), 0
            )
        }.getOrNull()
        log(context, "SYSTEM home=$homeName primeIsHome=${home?.activityInfo?.packageName == context.packageName} pinSupported=$pinSupported confirmHandlers=[$handlers] mainEnabled=${myActivity?.enabled}")
    }

    fun report(context: Context): String {
        val file = File(context.filesDir, FILE)
        val body = runCatching { if (file.exists()) file.readText() else "(no shortcut events yet)" }
            .getOrElse { "(failed to read log: ${it.javaClass.simpleName}: ${it.message})" }
        return buildString {
            appendLine("Prime Launcher — Add to Home debug")
            appendLine("Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Package: ${context.packageName}")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}")
            appendLine()
            append(body)
        }
    }

    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE).delete() }
        log(context, "LOG_CLEARED")
    }
}
