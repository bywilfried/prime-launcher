package com.bearinmind.launcher314.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Transparent gesture surface used only for the empty padding around the Home
 * grid. It is a separate composable so LauncherScreen's already-large root
 * modifier chain does not gain another pointer-input state machine.
 */
@Composable
fun HomeBackgroundLongPressArea(
    modifier: Modifier,
    enabled: Boolean,
    onLongPressInRoot: (Offset) -> Unit
) {
    var origin = Offset.Zero
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .onGloballyPositioned { origin = it.positionInRoot() }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onLongPress = { local -> onLongPressInRoot(origin + local) }
                )
            }
    )
}
