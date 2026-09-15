package com.rexosphere.haptictotp.input

import com.rexosphere.haptictotp.haptic.HapticSymbol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TapEntryTest {
    @Test
    fun classifierUsesThreshold() {
        val c = TapClassifier(longPressThresholdMillis = 300)
        assertEquals(HapticSymbol.SHORT, c.classify(0))
        assertEquals(HapticSymbol.SHORT, c.classify(299))
        assertEquals(HapticSymbol.LONG, c.classify(300))
        assertEquals(HapticSymbol.LONG, c.classify(2000))
    }

    @Test
    fun sessionCollectsUntilComplete() {
        val s = TapEntrySession(expectedLength = 3, classifier = TapClassifier(300))
        assertFalse(s.isComplete)
        assertEquals(HapticSymbol.SHORT, s.press(50))
        assertEquals(HapticSymbol.LONG, s.press(500))
        s.undo()
        assertEquals(listOf(HapticSymbol.SHORT), s.symbols)
        s.press(500)
        s.press(10)
        assertTrue(s.isComplete)
        assertEquals(".-.", s.pattern.toNotation())
        assertNull(s.press(10))
        s.clear()
        assertEquals(0, s.symbols.size)
    }
}
