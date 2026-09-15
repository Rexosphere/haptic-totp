package com.rexosphere.haptictotp.input

import com.rexosphere.haptictotp.haptic.HapticSymbol

/**
 * Turns a press duration into a [HapticSymbol]. A single fixed threshold is the
 * simplest thing that works; an adaptive threshold (e.g. relative to the median
 * of the user's recent taps) is a good follow-up task.
 */
class TapClassifier(val longPressThresholdMillis: Long = DEFAULT_LONG_PRESS_THRESHOLD_MILLIS) {

    fun classify(pressDurationMillis: Long): HapticSymbol =
        if (pressDurationMillis >= longPressThresholdMillis) HapticSymbol.LONG else HapticSymbol.SHORT

    companion object {
        const val DEFAULT_LONG_PRESS_THRESHOLD_MILLIS = 300L
    }
}
