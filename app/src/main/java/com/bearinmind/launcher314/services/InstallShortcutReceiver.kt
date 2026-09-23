package com.bearinmind.launcher314.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File

/** Receives "Add to Home Screen" shortcuts from browsers — Firefox checks for this receiver in the manifest before offering the option. */
class InstallShortcutReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        com.bearinmind.launcher314.data.ShortcutDebugLog.log(
            context,
            "LEGACY_RECEIVER action=${intent.action} hasIntent=${intent.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT) != null} name=${intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)}"
        )
        if (intent.action != "com.android.launcher.action.INSTALL_SHORTCUT") return

        // The home role is allowed to show UI for a user-requested shortcut.
        // Forward to an internal activity so legacy requests get the same choice.
        if (intent.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT) == null) return
        try {
            com.bearinmind.launcher314.data.ShortcutDebugLog.log(context, "LEGACY_RECEIVER forwarding_to_activity")
            context.startActivity(Intent(intent).apply {
                setClass(context, com.bearinmind.launcher314.activities.LegacyShortcutChoiceActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: RuntimeException) {
            com.bearinmind.launcher314.data.ShortcutDebugLog.log(context, "LEGACY_RECEIVER start_failed ${e.javaClass.simpleName}: ${e.message}")
            android.widget.Toast.makeText(context, com.bearinmind.launcher314.R.string.shortcut_add_failed,
                android.widget.Toast.LENGTH_LONG).show()
        }
    }

}

/** Package of the app that OPENS a shortcut (for its source badge): cached 3rd .meta line, else derived from the launch-intent URI and written back; null if undeterminable or the launcher itself. */
fun getShortcutSourcePackage(context: Context, shortcutId: String): String? {
    return try {
        val metaFile = File(context.filesDir, "shortcut_icons/$shortcutId.meta")
        if (!metaFile.exists()) return null
        val lines = metaFile.readLines()
        // 3rd line is the cached source package, when present.
        lines.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { return it }
        val intentUri = lines.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
        val launchIntent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME)
        val pkg = launchIntent.`package`
            ?: launchIntent.component?.packageName
            ?: context.packageManager.resolveActivity(launchIntent, 0)?.activityInfo?.packageName
        if (pkg != null && pkg != context.packageName) {
            // Cache it back so subsequent renders don't re-resolve.
            metaFile.writeText("${lines.getOrElse(0) { "Shortcut" }}\n$intentUri\n$pkg")
            pkg
        } else null
    } catch (_: Exception) {
        null
    }
}
