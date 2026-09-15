package com.rexosphere.haptictotp.output

import com.rexosphere.haptictotp.platform.AndroidPlatform

actual fun createHapticOutput(): HapticOutput = AndroidHapticOutput(AndroidPlatform.requireContext())
