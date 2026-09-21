package com.bearinmind.launcher314

import com.bearinmind.launcher314.data.*
import org.junit.Assert.*
import org.junit.Test

class HomeIconDefaultsTest {
    @Test fun newIconInheritsOptionsAndDiscardsStaleSizeOverrides() {
        val old = AppCustomization(hideLabel = false, iconSizePercent = 80,
            iconTextSizePercent = 75, customLabel = "Mail", iconShape = "circle")
        val current = AppCustomizations(mapOf("mail" to old))
        val added = withNewHomeIconDefaults(current, setOf("mail"), true).customizations.getValue("mail")
        assertTrue(added.hideLabel)
        assertNull(added.iconSizePercent)
        assertNull(added.iconTextSizePercent)
        assertEquals("Mail", added.customLabel)
        assertEquals("circle", added.iconShape)
        assertFalse(withNewHomeIconDefaults(current, setOf("mail"), false)
            .customizations.getValue("mail").hideLabel)
    }

    @Test fun movingAnIndividuallyEditedIconKeepsItsTextAndSize() {
        val before = HomeScreenData(apps = listOf(HomeScreenApp("mail", 0)))
        val after = HomeScreenData(dockApps = listOf(DockApp("mail", 2)))
        val edited = AppCustomization(hideLabel = false, iconSizePercent = 135, iconTextSizePercent = 110)
        val current = AppCustomizations(mapOf("mail" to edited))
        val added = homeIconCustomizationKeys(after) - existingHomeIconCustomizationKeys(before)
        assertTrue(added.isEmpty())
        assertEquals(current, withNewHomeIconDefaults(current, added, true))
    }

    @Test fun movingOutOfFolderKeepsIndividualEdits() {
        val before = HomeScreenData(folders = listOf(HomeFolder(id = "f", name = "Folder",
            position = 0, appPackageNames = listOf("mail|10"))))
        val after = before.copy(apps = listOf(HomeScreenApp("mail", 1, userSerial = 10L)))
        assertTrue((homeIconCustomizationKeys(after) - existingHomeIconCustomizationKeys(before)).isEmpty())
    }

    @Test fun addingAnotherIconDoesNotResetExistingIndividualEdits() {
        val edited = AppCustomization(hideLabel = false, iconSizePercent = 135)
        val result = withNewHomeIconDefaults(AppCustomizations(mapOf("mail" to edited)),
            setOf("browser", "folder_new"), true)
        assertEquals(edited, result.customizations["mail"])
        assertTrue(result.customizations.getValue("browser").hideLabel)
        assertTrue(result.customizations.getValue("folder_new").hideLabel)
    }
}
