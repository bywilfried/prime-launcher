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
