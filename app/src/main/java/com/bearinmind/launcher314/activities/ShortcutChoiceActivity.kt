package com.bearinmind.launcher314.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bearinmind.launcher314.MainActivity
import com.bearinmind.launcher314.R
import com.bearinmind.launcher314.data.*
import com.bearinmind.launcher314.helpers.LauncherAppsHelper
import com.bearinmind.launcher314.ui.widgets.WidgetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** The external request is accepted only when the user chooses its original shortcut.
 * A native app entry stores just the installed package/profile, with no publisher dependency. */
open class ShortcutChoiceActivity : ComponentActivity() {
    protected open val allowsLegacy = false
    private var legacy: Intent? = null
    private var request: LauncherApps.PinItemRequest? = null
    private lateinit var launcherApps: LauncherApps
    private var dialog: AlertDialog? = null
    private var busy = false
    private var pickerOpen = false
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcherApps = getSystemService(LauncherApps::class.java) ?: run { finish(); return }
        if (allowsLegacy && intent.action == "com.android.launcher.action.INSTALL_SHORTCUT" &&
            intent.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT) != null) {
            legacy = intent
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { finish(); return }
            val incoming = runCatching { launcherApps.getPinItemRequest(intent) }.getOrNull()
            if (incoming == null || !incoming.isValid ||
                incoming.requestType != LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                finish(); return
            }
            request = incoming
        }
        query = savedInstanceState?.getString("query").orEmpty()
        if (savedInstanceState?.getBoolean("picker") == true) showPicker() else showChoices()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("picker", pickerOpen)
        outState.putString("query", query)
        super.onSaveInstanceState(outState)
    }

    private fun showChoices() {
        pickerOpen = false
        val info = request?.shortcutInfo
        val legacyLaunch = legacy?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
        // Only a MAIN/LAUNCHER intent unambiguously represents a normal app.
        val targetPackage = if (info != null) shortcutAppTarget(info.`package`, info.id)
            else if (legacyLaunch?.action == Intent.ACTION_MAIN &&
                legacyLaunch.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                legacyLaunch.component?.packageName ?: legacyLaunch.`package`
            } else null
        val target = targetPackage?.let { pkg ->
            runCatching { launcherApps.getActivityList(pkg,
                info?.userHandle ?: android.os.Process.myUserHandle()).firstOrNull() }.getOrNull()
        }
        val source = info?.let {
            runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(it.`package`, 0)).toString()
            }.getOrDefault(it.`package`)
        }
        val name = info?.shortLabel ?: legacy?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: "Shortcut"
        val nativeLabel = if (target != null) getString(R.string.shortcut_add_app, target.label)
            else getString(R.string.shortcut_choose_app)
        dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.shortcut_add_title, name))
            .setItems(arrayOf(nativeLabel, if (source != null) getString(R.string.shortcut_keep, source) else getString(R.string.shortcut_keep_original))) { _, which ->
                if (which == 0) {
                    if (target == null) showPicker() else add(target)
                } else add(null)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun showPicker() {
        pickerOpen = true
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                LauncherAppsHelper.enumerateAllApps(this@ShortcutChoiceActivity)
                    .distinctBy { it.applicationInfo.packageName to it.user }
                    .sortedBy { it.label.toString().lowercase() }
            }
            if (isFinishing || isDestroyed) return@launch
            var visible = apps
            val input = EditText(this@ShortcutChoiceActivity).apply {
                hint = getString(R.string.shortcut_search_apps)
                setSingleLine(true)
            }
            val list = ListView(this@ShortcutChoiceActivity)
            fun filter(value: String) {
                visible = apps.filter { it.label.toString().contains(value, true) ||
                    it.applicationInfo.packageName.contains(value, true) }
                list.adapter = ArrayAdapter(this@ShortcutChoiceActivity,
                    android.R.layout.simple_list_item_1, visible.map {
                        val profile = LauncherAppsHelper.profileTypeFor(this@ShortcutChoiceActivity, it.user)
                        "${it.label} — ${it.applicationInfo.packageName}" +
                            if (it.user != android.os.Process.myUserHandle()) " ($profile)" else ""
                    })
            }
            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    query = s?.toString().orEmpty(); filter(query)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            val content = LinearLayout(this@ShortcutChoiceActivity).apply {
                orientation = LinearLayout.VERTICAL
                val padding = (16 * resources.displayMetrics.density).toInt()
                setPadding(padding, 0, padding, 0)
                addView(input)
                addView(list, LinearLayout.LayoutParams(-1, (320 * resources.displayMetrics.density).toInt()))
            }
            filter(query)
            input.setText(query)
            list.setOnItemClickListener { _, _, position, _ ->
                val app = visible[position]
                dialog?.dismiss()
                dialog = AlertDialog.Builder(this@ShortcutChoiceActivity)
                    .setTitle(getString(R.string.shortcut_add_app, app.label))
                    .setMessage(R.string.shortcut_native_explanation)
                    .setPositiveButton(R.string.shortcut_add) { _, _ -> add(app) }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> showPicker() }
                    .setOnCancelListener { showPicker() }.show()
            }
            dialog = AlertDialog.Builder(this@ShortcutChoiceActivity)
                .setTitle(R.string.shortcut_choose_app).setView(content)
                .setNegativeButton(android.R.string.cancel) { _, _ -> showChoices() }
                .setOnCancelListener { showChoices() }.show()
        }
    }

    /** A null app means keep the original pinned shortcut. */
    private fun add(app: LauncherActivityInfo?) {
        if (busy) return
        busy = true
        // All mutations happen synchronously after a user choice: rotation cannot
        // replay an in-flight acceptance or leave a detached coroutine adding twice.
        try {
            if (request?.isValid == false) { fail(R.string.shortcut_expired); return }
            if (app != null && !launcherApps.isActivityEnabled(app.componentName, app.user)) {
                fail(R.string.shortcut_app_unavailable); return
            }
            val data = loadHomeScreenData(this)
            val columns = getHomeGridSize(this)
            val rows = getHomeGridRows(this)
            val widgets = WidgetManager.loadPlacedWidgets(this)
            val occupied = (0..10).associateWith { page ->
                buildSet {
                    data.apps.filter { it.page == page }.forEach { add(it.position) }
                    data.folders.filter { it.page == page }.forEach { add(it.position) }
                    widgets.filter { it.page == page }.forEach { widget ->
                        for (row in widget.startRow until widget.startRow + widget.rowSpan)
                            for (column in widget.startColumn until widget.startColumn + widget.columnSpan)
                                add(row * columns + column)
                    }
                }
            }
            val space = firstShortcutSpace(columns, rows, occupied)
            if (space == null) { fail(R.string.shortcut_no_space); return }
            val info = request?.shortcutInfo
            val packageName: String
            val userSerial: Long?
            if (app != null) {
                packageName = app.applicationInfo.packageName
                userSerial = LauncherAppsHelper.serialFor(this, app.user)
            } else {
                packageName = "shortcut_${UUID.randomUUID()}"
                userSerial = null
                val dir = File(filesDir, "shortcut_icons").apply { mkdirs() }
                val meta = File(dir, "$packageName.meta")
                val icon = File(dir, "$packageName.png")
                try {
                    if (info != null) {
                        val serial = requireNotNull(getSystemService(UserManager::class.java)).getSerialNumberForUser(info.userHandle)
                        val name = info.shortLabel?.toString().orEmpty().replace('\n', ' ').replace('\r', ' ')
                        meta.writeText("$name\n\n${info.`package`}\n${info.id}\n$serial")
                        val drawable = runCatching {
                            launcherApps.getShortcutIconDrawable(info, resources.displayMetrics.densityDpi)
                        }.getOrNull() ?: runCatching { packageManager.getApplicationIcon(info.`package`) }.getOrNull()
                        if (drawable != null) {
                            val bitmap = drawableToBitmap(drawable)
                            try { saveBitmapToFile(bitmap, icon) } finally { bitmap.recycle() }
                        }
                        if (request?.accept() != true) {
                            meta.delete(); icon.delete(); fail(R.string.shortcut_expired); return
                        }
                    } else {
                        val original = legacy ?: error("Missing shortcut")
                        val launch = original.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
                            ?: error("Missing shortcut destination")
                        val name = original.getStringExtra(Intent.EXTRA_SHORTCUT_NAME).orEmpty()
                            .replace('\n', ' ').replace('\r', ' ')
                        val source = launch.component?.packageName ?: launch.`package` ?: ""
                        meta.writeText("$name\n${launch.toUri(Intent.URI_INTENT_SCHEME)}\n$source")
                        val supplied = original.getParcelableExtra<android.graphics.Bitmap>(Intent.EXTRA_SHORTCUT_ICON)
                        if (supplied != null) saveBitmapToFile(supplied, icon)
                        else {
                            val resource = original.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
                            if (resource != null) runCatching {
                                val res = packageManager.getResourcesForApplication(resource.packageName)
                                val id = res.getIdentifier(resource.resourceName, null, null)
                                val bitmap = drawableToBitmap(res.getDrawable(id, null))
                                try { saveBitmapToFile(bitmap, icon) } finally { bitmap.recycle() }
                            }
                        }
                    }
                } catch (e: Exception) {
                    meta.delete(); icon.delete(); throw e
                }
            }
            val entry = HomeScreenApp(packageName, space.second, space.first, userSerial)
            // Shared persistence applies exactly the same defaults as drawer additions.
            saveHomeScreenData(this, data.copy(apps = data.apps + entry))
            if (entry !in loadHomeScreenData(this).apps) {
                if (app == null) {
                    File(filesDir, "shortcut_icons/$packageName.meta").delete()
                    File(filesDir, "shortcut_icons/$packageName.png").delete()
                }
                fail(R.string.shortcut_add_failed); return
            }
            Toast.makeText(this, R.string.shortcut_added, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
        } catch (_: Exception) { fail(R.string.shortcut_add_failed) }
    }

    private fun fail(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
    }
}

/** Internal entry point: only the permission-checked legacy receiver forwards here. */
class LegacyShortcutChoiceActivity : ShortcutChoiceActivity() {
    override val allowsLegacy = true
}
