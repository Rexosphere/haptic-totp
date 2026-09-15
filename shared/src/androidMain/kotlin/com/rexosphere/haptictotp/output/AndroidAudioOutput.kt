package com.rexosphere.haptictotp.output

import android.util.Log
import com.rexosphere.haptictotp.haptic.Segment

/**
 * Android sound channel.
 *
 * STATUS: skeleton. Owner: sound task (see README "Work split").
 *
 * Implementation notes for the owner:
 *  - Simplest correct approach: generate a PCM sine wave with `AudioTrack` in
 *    `MODE_STATIC`, one buffer for the whole timeline (tone for `on` segments,
 *    zeros for `off`). 44.1 kHz, 16-bit mono is plenty. Apply a 5 ms fade in/out
 *    per tone to avoid clicks.
 *  - Alternative: `ToneGenerator` is quick to prototype with but has poor timing.
 *  - Use `AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY` so it respects accessibility volume.
 *  - Suspend until finished with `delay(segments.totalDurationMillis())`; `cancel()` stops and releases the track.
 *  - Sound leaks the code to bystanders. Keep it opt-in and suggest headphones (see docs/DESIGN.md).
 */
class AndroidAudioOutput : AudioOutput {

    // Flip to true once play() is implemented.
    override val isAvailable: Boolean
        get() = false

    override suspend fun play(segments: List<Segment>, tone: AudioTone) {
        // TODO(sound): synthesise and play the timeline.
        Log.w(TAG, "play() not implemented yet; ${segments.size} segments ignored")
    }

    override fun cancel() = Unit

    private companion object {
        const val TAG = "AndroidAudioOutput"
    }
}
