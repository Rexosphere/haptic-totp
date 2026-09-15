package com.rexosphere.haptictotp.haptic

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class HapticTimingTest {
    private val timing = HapticTiming(unitMillis = 100, leadInMillis = 0)

    @Test
    fun buildsMorseStyleTimeline() {
        val segments = HapticPattern.fromNotation(".-").toSegments(timing, groupSize = 4)
        assertEquals(
            listOf(Segment(true, 100), Segment(false, 100), Segment(true, 300)),
            segments,
        )
    }

    @Test
    fun usesLongerGapBetweenGroups() {
        val segments = HapticPattern.fromNotation("..").toSegments(timing, groupSize = 1)
        assertEquals(
            listOf(Segment(true, 100), Segment(false, 300), Segment(true, 100)),
            segments,
        )
    }

    @Test
    fun leadInIsFirstOffSegment() {
        val segments = HapticPattern.fromNotation(".").toSegments(HapticTiming(unitMillis = 100, leadInMillis = 250), 4)
        assertEquals(listOf(Segment(false, 250), Segment(true, 100)), segments)
        assertEquals(350, segments.totalDurationMillis())
    }

    @Test
    fun onOffTimingsAlternateStartingWithOff() {
        val segments = listOf(Segment(true, 100), Segment(false, 100), Segment(true, 300))
        assertContentEquals(longArrayOf(0, 100, 100, 300), segments.toOnOffTimings())

        val withLeadIn = listOf(Segment(false, 250), Segment(true, 100))
        assertContentEquals(longArrayOf(250, 100), withLeadIn.toOnOffTimings())
    }

    @Test
    fun fullDefaultPatternDuration() {
        // 8 symbols, groups of 4, all short, default timing: lead-in 300 + 8*200 + 6*200 + 1*600
        val segments = HapticPattern.fromNotation("........").toSegments(HapticTiming(), 4)
        assertEquals(300 + 1600 + 1200 + 600, segments.totalDurationMillis())
    }
}
