package com.rexosphere.haptictotp.output

/**
 * iOS channels are not implemented yet.
 *
 * Notes for whoever picks this up:
 *  - Vibration: `CoreHaptics` (`CHHapticEngine` + `CHHapticPattern` with continuous events
 *    built from `Segment`s) on iPhone 8+. `UIImpactFeedbackGenerator` is a fallback but cannot
 *    play a timed waveform.
 *  - Sound: `AVAudioEngine` with an `AVAudioSourceNode` generating a sine wave, or
 *    pre-render the timeline to a PCM buffer and play it with `AVAudioPlayerNode`.
 */
actual fun createHapticOutput(): HapticOutput = NoOpHapticOutput

actual fun createAudioOutput(): AudioOutput = NoOpAudioOutput
