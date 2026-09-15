package com.rexosphere.haptictotp.output

import com.rexosphere.haptictotp.haptic.Segment

/**
 * Vibration channel. Implemented per platform (see `androidMain`, `iosMain`).
 *
 * Contract for implementers:
 *  - [play] suspends until the whole timeline has been played or [cancel] was called,
 *    so the UI can show a "playing" state and prevent overlapping playback.
 *  - Segments with `on = true` vibrate for `durationMillis`; `on = false` is silence.
 *  - Use the same [Segment] list the audio channel gets, never re-derive timings.
 *  - Must be safe to call from the main thread (do the waiting with `delay`, not `Thread.sleep`).
 */
interface HapticOutput {
    /** False when the device has no vibrator or the platform implementation is not done yet. */
    val isAvailable: Boolean

    suspend fun play(segments: List<Segment>)

    fun cancel()
}

/** Placeholder used on platforms without an implementation. */
object NoOpHapticOutput : HapticOutput {
    override val isAvailable: Boolean = false
    override suspend fun play(segments: List<Segment>) = Unit
    override fun cancel() = Unit
}

/** Platform factory. Android needs the app context initialised first (see `AndroidPlatform`). */
expect fun createHapticOutput(): HapticOutput
