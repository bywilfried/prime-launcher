package com.bearinmind.launcher314.data

/** Package/folder keys are shared by the existing Home and Dock customization model. */
internal fun homeIconCustomizationKeys(data: HomeScreenData): Set<String> = buildSet {
    data.apps.forEach { add(it.packageName) }
    data.dockApps.forEach { add(it.packageName) }
    data.folders.forEach { add("folder_${it.id}") }
    data.dockFolders.forEach { add("folder_${it.id}") }
}

/** Moving an app out of a Home/Dock folder must retain its individual edits. */
internal fun existingHomeIconCustomizationKeys(data: HomeScreenData): Set<String> =
    homeIconCustomizationKeys(data) +
        (data.folders.flatMap { it.appPackageNames } + data.dockFolders.flatMap { it.appPackageNames })
            .filter { it.isNotEmpty() && !isFolderEntry(it) }
            .map { it.substringBefore('|') }

internal fun withNewHomeIconDefaults(
    current: AppCustomizations,
    addedKeys: Set<String>,
    hideText: Boolean
): AppCustomizations = current.copy(
    customizations = current.customizations.toMutableMap().apply {
        addedKeys.forEach { key ->
            this[key] = (this[key] ?: AppCustomization()).copy(
                hideLabel = hideText,
                // Null inherits the current Home/Dock size and global label size,
                // including later changes made in Settings.
                iconSizePercent = null,
                iconTextSizePercent = null
            )
        }
    }
)
