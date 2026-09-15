package com.rexosphere.haptictotp.haptic

import com.rexosphere.haptictotp.core.Hotp
import kotlin.test.Test
import kotlin.test.assertEquals

class HapticCodecTest {
    private val secret = "12345678901234567890".encodeToByteArray()

    @Test
    fun encodesLowBitsMostSignificantFirst() {
        // 0b0001_1000 = 24
        assertEquals("...--...", HapticCodec.encode(24, PatternConfig(length = 8)).toNotation())
        // 0b1110_1010 = 234
        assertEquals("---.-.-.", HapticCodec.encode(234, PatternConfig(length = 8)).toNotation())
        assertEquals("-.", HapticCodec.encode(2, PatternConfig(length = 2, groupSize = 1)).toNotation())
    }

    @Test
    fun ignoresBitsAboveLength() {
        assertEquals(
            HapticCodec.encode(24, PatternConfig(8)),
            HapticCodec.encode(24 + 256 * 5, PatternConfig(8)),
        )
    }

    @Test
    fun goldenPatternsFromRfc4226Counters() {
        // HOTP(counter 0) = 1284755224, low byte 24; HOTP(counter 1) = 1094287082, low byte 234.
        assertEquals("...--...", HapticCodec.encode(Hotp.truncatedValue(secret, 0)).toNotation())
        assertEquals("---.-.-.", HapticCodec.encode(Hotp.truncatedValue(secret, 1)).toNotation())
    }

    @Test
    fun decodeIsInverseOfEncode() {
        for (value in 0 until 256) {
            assertEquals(value, HapticCodec.decode(HapticCodec.encode(value, PatternConfig(8))))
        }
        assertEquals(1 shl 30, HapticCodec.decode(HapticCodec.encode(1 shl 30, PatternConfig(31, 1))))
    }

    @Test
    fun notationRoundTrip() {
        val p = HapticPattern.fromNotation(".-.. -.-.")
        assertEquals(".-..-.-.", p.toNotation())
        assertEquals(".-.. -.-.", p.toGroupedNotation(4))
        assertEquals("short, long, short, short, long, short, long, short", p.toSpokenText())
    }
}
