package com.rexosphere.haptictotp.input

import com.rexosphere.haptictotp.haptic.HapticPattern
import com.rexosphere.haptictotp.haptic.HapticSymbol

/**
 * Collects the symbols a user taps back until the expected length is reached.
 * Pure state machine, no UI, so it can be unit tested.
 */
class TapEntrySession(
    val expectedLength: Int,
    private val classifier: TapClassifier = TapClassifier(),
) {
    private val entered = ArrayList<HapticSymbol>(expectedLength)

    val symbols: List<HapticSymbol> get() = entered.toList()
    val isComplete: Boolean get() = entered.size >= expectedLength
    val pattern: HapticPattern get() = HapticPattern(symbols)

    /** Records a press. Returns the classified symbol, or null if the session was already complete. */
    fun press(durationMillis: Long): HapticSymbol? {
        if (isComplete) return null
        val symbol = classifier.classify(durationMillis)
        entered += symbol
        return symbol
    }

    fun undo() {
        if (entered.isNotEmpty()) entered.removeAt(entered.lastIndex)
    }

    fun clear() = entered.clear()
}
