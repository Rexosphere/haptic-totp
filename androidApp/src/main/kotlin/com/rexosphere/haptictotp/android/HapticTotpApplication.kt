package com.rexosphere.haptictotp.android

import android.app.Application
import com.rexosphere.haptictotp.platform.AndroidPlatform

class HapticTotpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)
    }
}
