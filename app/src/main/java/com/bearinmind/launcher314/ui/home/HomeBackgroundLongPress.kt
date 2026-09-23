package com.bearinmind.launcher314.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Handles long presses on the part of the Home surface that is not covered by
 * the icon/widget grid. Keeping this detector outside LauncherScreen avoids
 * adding another large gesture state machine to that composable.
 *
 * The detector never consumes a press. Existing children keep ownership of
 * icons, folders, widgets, the dock and pager gestures.
 */
fun Modifier.homeBackgroundLongPress(
    enabled: Boolean,
    isOutsideGrid: (Offset) -> Boolean,
    onLongPress: (Offset) -> Unit
): Modifier = if (!enabled) {
    this
} else {
    pointerInput(enabled) {
        detectTapGestures(
            onLongPress = { position ->
                if (isOutsideGrid(position)) onLongPress(position)
            }
        )
    }
}
