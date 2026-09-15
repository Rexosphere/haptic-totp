package com.rexosphere.haptictotp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.time.TimeSource

/**
 * Large press area. Reports the press duration in milliseconds on release; the
 * caller classifies it as short or long. This is the "Gesture" input method
 * from the Haptic2FA study. Note that screen readers intercept raw touches, so
 * the screen also offers explicit Short / Long buttons.
 */
@Composable
fun TapPad(
    enabled: Boolean,
    onPress: (durationMillis: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val background = when {
        !enabled -> colors.surfaceVariant
        pressed -> colors.primaryContainer
        else -> colors.secondaryContainer
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(background, RoundedCornerShape(24.dp))
            .border(3.dp, colors.primary, RoundedCornerShape(24.dp))
            .semantics {
                contentDescription = "Tap pad. Short tap for a short symbol, press and hold for a long symbol."
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val start = TimeSource.Monotonic.markNow()
                        tryAwaitRelease()
                        pressed = false
                        onPress(start.elapsedNow().inWholeMilliseconds)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (enabled) "Tap here" else "Done",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSecondaryContainer,
        )
    }
}
