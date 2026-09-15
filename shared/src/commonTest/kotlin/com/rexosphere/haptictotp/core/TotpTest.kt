package com.rexosphere.haptictotp.core

import kotlin.test.Test
import kotlin.test.assertEquals

/** RFC 6238 Appendix B test vectors (30 s step, 8 digits). */
class TotpTest {
    private val seed20 = "12345678901234567890".encodeToByteArray()
    private val seed32 = "12345678901234567890123456789012".encodeToByteArray()
    private val seed64 = "1234567890123456789012345678901234567890123456789012345678901234".encodeToByteArray()

    private data class Vector(val time: Long, val sha1: String, val sha256: String, val sha512: String)

    private val vectors = listOf(
        Vector(59, "94287082", "46119246", "90693936"),
        Vector(1111111109, "07081804", "68084774", "25091201"),
        Vector(1111111111, "14050471", "67062674", "99943326"),
        Vector(1234567890, "89005924", "91819424", "93441116"),
        Vector(2000000000, "69279037", "90698825", "38618901"),
        Vector(20000000000, "65353130", "77737706", "47863826"),
    )

    @Test
    fun matchesRfc6238Vectors() {
        for (v in vectors) {
            assertEquals(v.sha1, Totp(TotpConfig(30, HmacAlgorithm.SHA1)).digits(seed20, v.time, 8), "SHA1 @ ${v.time}")
            assertEquals(v.sha256, Totp(TotpConfig(30, HmacAlgorithm.SHA256)).digits(seed32, v.time, 8), "SHA256 @ ${v.time}")
            assertEquals(v.sha512, Totp(TotpConfig(30, HmacAlgorithm.SHA512)).digits(seed64, v.time, 8), "SHA512 @ ${v.time}")
        }
    }

    @Test
    fun sixtySecondStepCounterAndRemaining() {
        val totp = Totp(TotpConfig(stepSeconds = 60))
        assertEquals(0, totp.counterAt(0))
        assertEquals(0, totp.counterAt(59))
        assertEquals(1, totp.counterAt(60))
        assertEquals(60, totp.secondsRemaining(0))
        assertEquals(1, totp.secondsRemaining(59))
        assertEquals(60, totp.secondsRemaining(60))
        assertEquals(18518518, totp.counterAt(1111111111))
    }

    @Test
    fun defaultConfigIsSixtySeconds() {
        assertEquals(60, TotpConfig().stepSeconds)
        assertEquals(HmacAlgorithm.SHA1, TotpConfig().algorithm)
    }
}
