package com.rexosphere.haptictotp.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Base32Test {
    private val rfcSecret = "12345678901234567890".encodeToByteArray()

    @Test
    fun decodesRfcSecret() {
        assertContentEquals(rfcSecret, Base32.decode("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"))
    }

    @Test
    fun encodesRfcSecret() {
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", Base32.encode(rfcSecret))
    }

    @Test
    fun isLenientAboutCaseSpacesAndPadding() {
        assertContentEquals(rfcSecret, Base32.decode("gezd gnbv gy3t qojq gezd gnbv gy3t qojq=="))
    }

    @Test
    fun roundTripsArbitraryLengths() {
        for (len in 0..40) {
            val bytes = ByteArray(len) { (it * 37 + 11).toByte() }
            if (len == 0) continue
            assertContentEquals(bytes, Base32.decode(Base32.encode(bytes, padding = true)))
            assertContentEquals(bytes, Base32.decode(Base32.encode(bytes)))
        }
    }

    @Test
    fun knownVector() {
        assertEquals("JBSWY3DPEHPK3PXP", Base32.encode(byteArrayOf(72, 101, 108, 108, 111, 33, 0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())))
    }

    @Test
    fun rejectsInvalidCharacters() {
        assertFailsWith<IllegalArgumentException> { Base32.decode("ABC1") }
        assertFailsWith<IllegalArgumentException> { Base32.decode("") }
    }
}
