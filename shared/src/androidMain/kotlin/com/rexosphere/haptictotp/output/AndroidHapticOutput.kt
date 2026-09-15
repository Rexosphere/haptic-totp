package com.rexosphere.haptictotp.output

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.rexosphere.haptictotp.haptic.Segment

/**
 * Android vibration channel.
 *
 * STATUS: skeleton. Owner: vibration task (see README "Work split").
 *
 * Implementation notes for the owner:
 *  - `segments.toOnOffTimings()` already produces the `timings` array that
 *    `VibrationEffect.createWaveform(timings, -1)` expects (off/on alternating, starting with off).
 *  - minSdk is 26, so `VibrationEffect` is always available. On API 31+ prefer
 *    `VibrationEffect.startComposition()` with `PRIMITIVE_CLICK`/`PRIMITIVE_TICK` if
 *    `vibrator.areAllPrimitivesSupported(...)`, because primitives feel crisper than a plain waveform.
 *  - Amplitude: use `createWaveform(timings, amplitudes, -1)` when `vibrator.hasAmplitudeControl()`
 *    so short and long symbols can also differ in strength.
 *  - Suspend until finished: `delay(segments.totalDurationMillis())` after starting playback,
 *    and make [cancel] call `vibrator.cancel()`.
 *  - Do not vibrate while another playback is running; the UI guards this too.
 */
class AndroidHapticOutput(context: Context) : HapticOutput {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Flip to `vibrator?.hasVibrator() == true` once play() is implemented.
    override val isAvailable: Boolean
        get() = false

    override suspend fun play(segments: List<Segment>) {
        // TODO(vibration): build a VibrationEffect from `segments` and play it on `vibrator`.
        Log.w(TAG, "play() not implemented yet; ${segments.size} segments ignored")
    }

    override fun cancel() {
        vibrator?.cancel()
    }

    private companion object {
        const val TAG = "AndroidHapticOutput"
    }
}
