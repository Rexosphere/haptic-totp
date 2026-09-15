package com.rexosphere.haptictotp.haptic

import com.rexosphere.haptictotp.core.Totp
import com.rexosphere.haptictotp.core.TotpConfig
import com.rexosphere.haptictotp.platform.EpochClock
import com.rexosphere.haptictotp.platform.SystemEpochClock

/**
 * Facade that ties TOTP and the haptic codec together. One instance per account.
 *
 * Generating: [patternAt] / [currentPattern].
 * Verifying:  [verify] accepts the pattern for the current step and, by default,
 *             one step either side to tolerate clock drift and slow entry.
 */
class HapticTotp(
    private val secret: ByteArray,
    val totp: Totp = Totp(TotpConfig()),
    val patternConfig: PatternConfig = PatternConfig(),
) {
    fun patternForCounter(counter: Long): HapticPattern =
        HapticCodec.encode(totp.truncatedValueForCounter(secret, counter), patternConfig)

    fun patternAt(epochSeconds: Long): HapticPattern =
        patternForCounter(totp.counterAt(epochSeconds))

    fun currentPattern(clock: EpochClock = SystemEpochClock): HapticPattern =
        patternAt(clock.nowEpochSeconds())

    fun secondsRemaining(epochSeconds: Long): Long = totp.secondsRemaining(epochSeconds)

    /** Classic 6-digit code for the same moment; handy when debugging against another authenticator. */
    fun digitsAt(epochSeconds: Long): String = totp.digits(secret, epochSeconds)

    fun verify(
        candidate: HapticPattern,
        epochSeconds: Long,
        allowedSkewSteps: Int = DEFAULT_SKEW_STEPS,
    ): Boolean {
        if (candidate.length != patternConfig.length) return false
        val counter = totp.counterAt(epochSeconds)
        return (-allowedSkewSteps..allowedSkewSteps).any { skew ->
            patternForCounter(counter + skew) == candidate
        }
    }

    companion object {
        const val DEFAULT_SKEW_STEPS = 1
    }
}
