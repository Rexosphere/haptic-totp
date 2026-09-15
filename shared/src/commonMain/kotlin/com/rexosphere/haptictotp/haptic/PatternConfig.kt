package com.rexosphere.haptictotp.haptic

/**
 * How many symbols a code has and how they are chunked.
 *
 * Security note: with a 2-symbol alphabet each symbol carries one bit, so
 * [length] = 8 gives 256 possible codes per time step. That is far below the
 * ~20 bits of a 6-digit TOTP, which is why a verifier MUST rate-limit attempts
 * (see docs/DESIGN.md, "Entropy and brute force"). Raise [length] to trade
 * usability for security; the codec supports up to 31.
 */
data class PatternConfig(
    val length: Int = DEFAULT_LENGTH,
    val groupSize: Int = DEFAULT_GROUP_SIZE,
) {
    init {
        require(length in 1..MAX_LENGTH) { "length must be 1..$MAX_LENGTH" }
        require(groupSize in 1..length) { "groupSize must be 1..length" }
    }

    companion object {
        const val DEFAULT_LENGTH = 8
        const val DEFAULT_GROUP_SIZE = 4
        const val MAX_LENGTH = 31
    }
}
