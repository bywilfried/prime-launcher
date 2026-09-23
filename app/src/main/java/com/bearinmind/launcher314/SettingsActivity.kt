package com.bearinmind.launcher314

/**
 * Normal app-task entry point for Prime settings.
 *
 * MainActivity remains the HOME activity and is excluded from Recents; keeping
 * settings on a distinct Activity lets Android represent settings in Recents
 * without representing the launcher itself.
 */
class SettingsActivity : MainActivity()
