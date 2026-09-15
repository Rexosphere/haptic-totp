package com.rexosphere.haptictotp.output

actual fun createAudioOutput(): AudioOutput = AndroidAudioOutput()
