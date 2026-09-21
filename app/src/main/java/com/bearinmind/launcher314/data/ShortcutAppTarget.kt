package com.bearinmind.launcher314.data

/** Only decode conventions from known publishers. Labels and publisher packages
 * alone do not identify the destination (websites and deep links use them too). */
internal fun shortcutAppTarget(publisher: String, shortcutId: String): String? {
    if (publisher != "com.tk.quicksearch" && publisher != "com.tk.quicksearch.debug") return null
    if (!shortcutId.startsWith("app_")) return null
    return shortcutId.removePrefix("app_").takeIf {
        it.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))
    }
}

/** Null means full: never fall back to an occupied cell. */
internal fun firstShortcutSpace(columns: Int, rows: Int, occupied: Map<Int, Set<Int>>): Pair<Int, Int>? {
    if (columns <= 0 || rows <= 0) return null
    for (page in 0..10) {
        val position = (0 until columns * rows).firstOrNull { it !in occupied[page].orEmpty() }
        if (position != null) return page to position
    }
    return null
}
