package com.bearinmind.launcher314

import com.bearinmind.launcher314.data.firstShortcutSpace
import com.bearinmind.launcher314.data.shortcutAppTarget
import org.junit.Assert.*
import org.junit.Test

class ShortcutAppTargetTest {
    @Test fun quickSearchAppCanBeIdentified() {
        assertEquals("org.mozilla.firefox", shortcutAppTarget("com.tk.quicksearch", "app_org.mozilla.firefox"))
        assertEquals("org.mozilla.firefox", shortcutAppTarget("com.tk.quicksearch.debug", "app_org.mozilla.firefox"))
    }
    @Test fun unknownPublishersAndNonAppShortcutsRequireManualChoice() {
        assertNull(shortcutAppTarget("other.search", "app_org.mozilla.firefox"))
        assertNull(shortcutAppTarget("com.tk.quicksearch", "contact_123"))
        assertNull(shortcutAppTarget("com.tk.quicksearch", "shortcut_app_org.mozilla.firefox"))
        assertNull(shortcutAppTarget("com.tk.quicksearch", "app_"))
        assertNull(shortcutAppTarget("com.tk.quicksearch", "app_com.example/Activity"))
    }
    @Test fun placementSkipsOccupiedCellsAndPages() {
        assertEquals(0 to 2, firstShortcutSpace(2, 2, mapOf(0 to setOf(0, 1, 3))))
        assertEquals(1 to 1, firstShortcutSpace(2, 2, mapOf(0 to setOf(0, 1, 2, 3), 1 to setOf(0))))
        assertEquals(0 to 0, firstShortcutSpace(2, 2, mapOf(1 to setOf(0, 1, 2, 3))))
    }
    @Test fun fullOrInvalidGridDoesNotOverwriteAnIcon() {
        assertNull(firstShortcutSpace(2, 2, (0..10).associateWith { setOf(0, 1, 2, 3) }))
        assertNull(firstShortcutSpace(0, 4, emptyMap()))
    }
}
