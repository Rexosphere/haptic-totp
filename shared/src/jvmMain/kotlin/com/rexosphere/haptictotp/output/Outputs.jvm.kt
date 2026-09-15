package com.rexosphere.haptictotp.output

actual fun createHapticOutput(): HapticOutput = NoOpHapticOutput

actual fun createAudioOutput(): AudioOutput = NoOpAudioOutput
