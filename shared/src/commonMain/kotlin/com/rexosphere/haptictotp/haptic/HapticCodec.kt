package com.rexosphere.haptictotp.haptic

/**
 * Maps the 31-bit HOTP truncated value to a [HapticPattern].
 *
 * Encoding: take the lowest [PatternConfig.length] bits of the value, most
 * significant bit first; `1` = [HapticSymbol.LONG], `0` = [HapticSymbol.SHORT].
 *
 * This is the analogue of `value mod 10^digits` in a classic authenticator.
 * Anyone writing a verifier in another language only needs this function,
 * HOTP and Base32 to interoperate.
 */
object HapticCodec {
    fun encode(truncatedValue: Int, config: PatternConfig = PatternConfig()): HapticPattern {
        require(truncatedValue >= 0) { "truncatedValue must be the non-negative 31-bit HOTP value" }
        val symbols = (config.length - 1 downTo 0).map { bit ->
            if ((truncatedValue shr bit) and 1 == 1) HapticSymbol.LONG else HapticSymbol.SHORT
        }
        return HapticPattern(symbols)
    }

    /** Inverse of [encode]; useful for tests and for a numeric fallback display. */
    fun decode(pattern: HapticPattern): Int =
        pattern.symbols.fold(0) { acc, s -> (acc shl 1) or if (s == HapticSymbol.LONG) 1 else 0 }
}
