package com.rexosphere.haptictotp.platform

/** Injectable wall clock so TOTP logic is testable with fixed times. */
fun interface EpochClock {
    fun nowEpochSeconds(): Long
}

/** Milliseconds since 1970-01-01T00:00:00Z from the platform wall clock. */
expect fun currentEpochMillis(): Long

object SystemEpochClock : EpochClock {
    override fun nowEpochSeconds(): Long = currentEpochMillis() / 1000
}
