package com.rexosphere.haptictotp.core

/**
 * RFC 4226 HOTP. The interesting output for this project is [truncatedValue]:
 * the 31-bit integer produced by "dynamic truncation". Classic authenticators
 * reduce it to 6 decimal digits; we reduce it to a haptic pattern instead
 * (see [com.rexosphere.haptictotp.haptic.HapticCodec]).
 */
object Hotp {
    /** Returns the 31-bit dynamically truncated value described in RFC 4226 §5.3. */
    fun truncatedValue(
        secret: ByteArray,
        counter: Long,
        algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    ): Int {
        val message = ByteArray(8) { i -> (counter ushr (56 - 8 * i)).toByte() }
        val hash = Hmac.compute(algorithm, secret, message)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        return ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)
    }

    /** Classic numeric code, kept for interoperability tests and debugging. */
    fun digits(
        secret: ByteArray,
        counter: Long,
        digits: Int = 6,
        algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    ): String {
        require(digits in 1..9) { "digits must be 1..9" }
        var modulus = 1
        repeat(digits) { modulus *= 10 }
        return (truncatedValue(secret, counter, algorithm) % modulus).toString().padStart(digits, '0')
    }
}
