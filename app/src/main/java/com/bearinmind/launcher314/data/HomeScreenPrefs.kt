package com.bearinmind.launcher314.data

import android.content.Context

// SharedPreferences keys for Home Screen settings
private const val HOME_PREFS_NAME = "home_screen_settings"
private const val HOME_KEY_GRID_COLUMNS = "home_grid_columns"
private const val HOME_KEY_GRID_ROWS = "home_grid_rows"
private const val HOME_KEY_ICON_SIZE = "home_icon_size"
private const val HOME_KEY_ICON_SIZE_PERCENT = "home_icon_size_percent"
private const val HOME_KEY_DOCK_COLUMNS = "home_dock_columns"
private const val HOME_KEY_DOCK_ENABLED = "home_dock_enabled"
private const val HOME_KEY_DOCK_PAGES = "home_dock_pages"

// Grid columns (X)
fun getHomeGridSize(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(HOME_KEY_GRID_COLUMNS, 4)
}

fun setHomeGridSize(context: Context, size: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_GRID_COLUMNS, size).apply()
}

// Grid rows (Y)
fun getHomeGridRows(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(HOME_KEY_GRID_ROWS, 6)
}

fun setHomeGridRows(context: Context, rows: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_GRID_ROWS, rows).apply()
}

fun getHomeIconSize(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(HOME_KEY_ICON_SIZE, 48)
}

fun setHomeIconSize(context: Context, size: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_ICON_SIZE, size).apply()
}

// Icon size shared between home screen and app drawer (unified setting)
fun getHomeIconSizePercent(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    // Migrate once from the formerly shared Drawer value so existing users keep
    // their current visual size when Home and Drawer become independent.
    if (!prefs.contains(HOME_KEY_ICON_SIZE_PERCENT)) {
        val migrated = getDrawerIconSizePercent(context)
        prefs.edit().putInt(HOME_KEY_ICON_SIZE_PERCENT, migrated).apply()
        return migrated
    }
    return prefs.getInt(HOME_KEY_ICON_SIZE_PERCENT, 100)
}

fun setHomeIconSizePercent(context: Context, percent: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_ICON_SIZE_PERCENT, percent).apply()
}

private const val HOME_KEY_DOCK_ICON_SIZE_PERCENT = "home_dock_icon_size_percent"

fun getDockIconSizePercent(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    // Same migration rule: Dock initially inherits the user's existing Home size,
    // then becomes fully independent after its first stored value.
    if (!prefs.contains(HOME_KEY_DOCK_ICON_SIZE_PERCENT)) {
        val migrated = getHomeIconSizePercent(context)
        prefs.edit().putInt(HOME_KEY_DOCK_ICON_SIZE_PERCENT, migrated).apply()
        return migrated
    }
    return prefs.getInt(HOME_KEY_DOCK_ICON_SIZE_PERCENT, 100)
}

fun setDockIconSizePercent(context: Context, percent: Int) {
    context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putInt(HOME_KEY_DOCK_ICON_SIZE_PERCENT, percent).apply()
}

// Dock columns
fun getDockColumns(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(HOME_KEY_DOCK_COLUMNS, 5) // Default 5 dock slots
}

fun setDockColumns(context: Context, columns: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_DOCK_COLUMNS, columns).apply()
}

fun getDockEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(HOME_KEY_DOCK_ENABLED, true)
}

fun setDockEnabled(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(HOME_KEY_DOCK_ENABLED, enabled).apply()
}

// Number of dock pages (default 1, max 5). When > 1, the dock becomes
// a HorizontalPager and apps/folders carry a `page` field.
fun getDockPages(context: Context): Int {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(HOME_KEY_DOCK_PAGES, 1).coerceIn(1, 5)
}

fun setDockPages(context: Context, pages: Int) {
    val prefs = context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(HOME_KEY_DOCK_PAGES, pages.coerceIn(1, 5)).apply()
}


// Experimental: when enabled, external Add-to-Home requests show Prime's
// app-vs-original choice UI. Disabled by default keeps the original shortcut.
private const val HOME_KEY_EXTERNAL_ADD_TO_HOME_HANDLING = "external_add_to_home_handling"

fun getExternalAddToHomeHandling(context: Context): Boolean {
    return context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(HOME_KEY_EXTERNAL_ADD_TO_HOME_HANDLING, false)
}

fun setExternalAddToHomeHandling(context: Context, enabled: Boolean) {
    context.getSharedPreferences(HOME_PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(HOME_KEY_EXTERNAL_ADD_TO_HOME_HANDLING, enabled).apply()
}
