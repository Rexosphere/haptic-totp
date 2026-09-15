package com.rexosphere.haptictotp.core

import kotlin.test.Test
import kotlin.test.assertEquals

/** RFC 4226 Appendix D test vectors. */
class HotpTest {
    private val secret = "12345678901234567890".encodeToByteArray()

    @Test
    fun truncatedValuesMatchRfc4226() {
        val expected = listOf(
            1284755224, 1094287082, 137359152, 1726969429, 1640338314,
            868254676, 1918287922, 82162583, 673399871, 645520489,
        )
        expected.forEachIndexed { counter, value ->
            assertEquals(value, Hotp.truncatedValue(secret, counter.toLong()), "counter $counter")
        }
    }

    @Test
    fun digitsMatchRfc4226() {
        val expected = listOf(
            "755224", "287082", "359152", "969429", "338314",
            "254676", "287922", "162583", "399871", "520489",
        )
        expected.forEachIndexed { counter, code ->
            assertEquals(code, Hotp.digits(secret, counter.toLong()), "counter $counter")
        }
    }
}
