package com.rexosphere.haptictotp.core

/**
 * RFC 6238 TOTP parameters. The project default is a 60 second step, which is
 * friendlier for blind and low-vision users than the usual 30 seconds: feeling a
 * pattern and tapping it back takes longer than glancing at six digits.
 */
data class TotpConfig(
    val stepSeconds: Long = DEFAULT_STEP_SECONDS,
    val algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    /** Unix time to start counting steps from (T0 in the RFC). Practically always 0. */
    val t0EpochSeconds: Long = 0,
) {
    init {
        require(stepSeconds > 0) { "stepSeconds must be positive" }
    }

    companion object {
        const val DEFAULT_STEP_SECONDS = 60L
    }
}

class Totp(val config: TotpConfig = TotpConfig()) {

    /** The HOTP counter (`T` in RFC 6238) for a given moment. */
    fun counterAt(epochSeconds: Long): Long =
        (epochSeconds - config.t0EpochSeconds).floorDiv(config.stepSeconds)

    /** Seconds until the counter advances. Always in `1..stepSeconds`. */
    fun secondsRemaining(epochSeconds: Long): Long =
        config.stepSeconds - (epochSeconds - config.t0EpochSeconds).mod(config.stepSeconds)

    fun truncatedValue(secret: ByteArray, epochSeconds: Long): Int =
        Hotp.truncatedValue(secret, counterAt(epochSeconds), config.algorithm)

    fun truncatedValueForCounter(secret: ByteArray, counter: Long): Int =
        Hotp.truncatedValue(secret, counter, config.algorithm)

    fun digits(secret: ByteArray, epochSeconds: Long, digits: Int = 6): String =
        Hotp.digits(secret, counterAt(epochSeconds), digits, config.algorithm)
}
