package com.bearinmind.launcher314.ui.home

import android.content.Context
import java.util.Locale

object HomePerformanceDiagnostics {
    private const val PREFS = "prime_home_perf"
    private const val KEY_REPORT = "report"
    private const val KEY_COUNT = "count"\n    private const val KEY_EVENTS = "events"

    fun recordSwipe(context: Context, frames: Int, slowFrames: Int, worstFrameMs: Float) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        val line = String.format(Locale.US, "Swipe %d: frames=%d, slowFramesOver20ms=%d, worstFrameMs=%.1f", count, frames, slowFrames, worstFrameMs)
        val old = prefs.getString(KEY_REPORT, "").orEmpty()
        val lines = (old.lines().filter { it.isNotBlank() } + line).takeLast(30)
        prefs.edit().putInt(KEY_COUNT, count).putString(KEY_REPORT, lines.joinToString("\n")).apply()
    }

    fun recordEvent(context: Context, name: String, durationMs: Float) {\n        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)\n        val line = String.format(Locale.US, "%s=%.1fms", name, durationMs)\n        val old = prefs.getString(KEY_EVENTS, "").orEmpty()\n        val lines = (old.lines().filter { it.isNotBlank() } + line).takeLast(60)\n        prefs.edit().putString(KEY_EVENTS, lines.joinToString("\\n")).apply()\n    }\n\n    fun report(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val body = prefs.getString(KEY_REPORT, "").orEmpty()
        return if (body.isBlank()) "No Home swipes recorded yet." else
            "Prime Launcher Home performance\n" + prefs.getInt(KEY_COUNT, 0) + " swipes recorded\n\n" + body
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
