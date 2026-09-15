package com.rexosphere.haptictotp.haptic

import com.rexosphere.haptictotp.core.Totp
import com.rexosphere.haptictotp.core.TotpConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HapticTotpTest {
    private val secret = "12345678901234567890".encodeToByteArray()
    private val hapticTotp = HapticTotp(secret, Totp(TotpConfig(stepSeconds = 60)), PatternConfig(8, 4))

    @Test
    fun patternIsStableWithinAStepAndChangesAcrossSteps() {
        assertEquals(hapticTotp.patternAt(0), hapticTotp.patternAt(59))
        assertEquals("...--...", hapticTotp.patternAt(30).toNotation()) // counter 0
        assertEquals("---.-.-.", hapticTotp.patternAt(60).toNotation()) // counter 1
    }

    @Test
    fun verifyAcceptsCurrentAndAdjacentSteps() {
        val now = 60L * 1000 + 17 // counter 1000
        val current = hapticTotp.patternAt(now)
        val previous = hapticTotp.patternForCounter(999)
        val next = hapticTotp.patternForCounter(1001)
        val farAway = hapticTotp.patternForCounter(1003)

        assertTrue(hapticTotp.verify(current, now))
        assertTrue(hapticTotp.verify(previous, now))
        assertTrue(hapticTotp.verify(next, now))
        assertFalse(hapticTotp.verify(current, now, allowedSkewSteps = 0) && hapticTotp.verify(farAway, now))
    }

    @Test
    fun verifyRejectsWrongLength() {
        val now = 12345L
        val short = HapticPattern(hapticTotp.patternAt(now).symbols.dropLast(1))
        assertFalse(hapticTotp.verify(short, now))
    }

    @Test
    fun digitsMatchClassicTotp() {
        assertEquals(Totp(TotpConfig(60)).digits(secret, 90), hapticTotp.digitsAt(90))
    }
}
