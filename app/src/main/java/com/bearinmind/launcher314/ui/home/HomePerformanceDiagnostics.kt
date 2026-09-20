package com.bearinmind.launcher314.ui.home

import android.content.Context
import java.util.Locale

object HomePerformanceDiagnostics {
    private const val PREFS = "prime_home_perf"
    private const val KEY_REPORT = "report"
    private const val KEY_COUNT = "count"
    private const val KEY_EVENTS = "events"

    fun recordSwipe(context: Context, frames: Int, slowFrames: Int, worstFrameMs: Float) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        val line = String.format(Locale.US, "Swipe %d: frames=%d, slowFramesOver20ms=%d, worstFrameMs=%.1f", count, frames, slowFrames, worstFrameMs)
        val old = prefs.getString(KEY_REPORT, "").orEmpty()
        val lines = (old.lines().filter { it.isNotBlank() } + line).takeLast(30)
        prefs.edit().putInt(KEY_COUNT, count).putString(KEY_REPORT, lines.joinToString("\n")).apply()
    }

    fun recordEvent(context: Context, name: String, durationMs: Float) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val line = String.format(Locale.US, "%s=%.1fms", name, durationMs)
        val old = prefs.getString(KEY_EVENTS, "").orEmpty()
        val lines = (old.lines().filter { it.isNotBlank() } + line).takeLast(60)
        prefs.edit().putString(KEY_EVENTS, lines.joinToString("\n")).apply()
    }

    fun report(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val body = prefs.getString(KEY_REPORT, "").orEmpty()
        val events = prefs.getString(KEY_EVENTS, "").orEmpty()
        if (body.isBlank() && events.isBlank()) return "No Home swipes recorded yet."
        return "Prime Launcher Home performance\n" +
            prefs.getInt(KEY_COUNT, 0) + " swipes recorded\n\n" +
            body +
            if (events.isBlank()) "" else "\n\nTimed events (>= 2ms)\n" + events
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
