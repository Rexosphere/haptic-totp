package com.rexosphere.haptictotp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rexosphere.haptictotp.haptic.HapticPattern
import com.rexosphere.haptictotp.haptic.HapticSymbol

/**
 * Sighted-developer view of a pattern: dot = circle, dash = wide pill.
 * [revealToScreenReader] should be false for the *expected* code (reading the
 * secret code aloud defeats the purpose); true for what the user entered.
 */
@Composable
fun PatternVisual(
    pattern: HapticPattern,
    groupSize: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    placeholderSlots: Int = 0,
    revealToScreenReader: Boolean = false,
) {
    val description = if (revealToScreenReader) {
        if (pattern.length == 0) "Nothing entered yet" else "Entered: ${pattern.toSpokenText()}"
    } else {
        "Current code, ${pattern.length} symbols. Use Feel or Hear to receive it."
    }
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pattern.symbols.forEachIndexed { index, symbol ->
            if (index > 0) Spacer(Modifier.width(if (index % groupSize == 0) 18.dp else 6.dp))
            SymbolShape(symbol, color)
        }
        val missing = (placeholderSlots - pattern.length).coerceAtLeast(0)
        repeat(missing) { i ->
            val index = pattern.length + i
            if (index > 0) Spacer(Modifier.width(if (index % groupSize == 0) 18.dp else 6.dp))
            Box(
                Modifier
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
        }
    }
}

@Composable
private fun SymbolShape(symbol: HapticSymbol, color: Color) {
    when (symbol) {
        HapticSymbol.SHORT -> Box(Modifier.size(14.dp).background(color, CircleShape))
        HapticSymbol.LONG -> Box(Modifier.width(38.dp).height(14.dp).background(color, RoundedCornerShape(7.dp)))
    }
}
