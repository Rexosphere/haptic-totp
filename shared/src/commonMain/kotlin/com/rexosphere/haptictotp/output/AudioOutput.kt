package com.rexosphere.haptictotp.output

import com.rexosphere.haptictotp.haptic.Segment

/** Tone settings for the sound channel. Kept simple on purpose; extend as needed. */
data class AudioTone(
    val frequencyHz: Int = 660,
    /** 0.0 .. 1.0 */
    val volume: Float = 0.8f,
)

/**
 * Sound channel: plays a tone for every `on` segment and silence for `off`.
 * Same contract as [HapticOutput]: suspend until finished, honour [cancel],
 * consume the shared [Segment] timeline so sound and vibration stay in sync.
 *
 * Privacy note: sound can be overheard. The UI should default to vibration and
 * offer sound as an opt-in (headphones recommended). See docs/DESIGN.md.
 */
interface AudioOutput {
    val isAvailable: Boolean

    suspend fun play(segments: List<Segment>, tone: AudioTone = AudioTone())

    fun cancel()
}

object NoOpAudioOutput : AudioOutput {
    override val isAvailable: Boolean = false
    override suspend fun play(segments: List<Segment>, tone: AudioTone) = Unit
    override fun cancel() = Unit
}

expect fun createAudioOutput(): AudioOutput
