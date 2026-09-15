package com.rexosphere.haptictotp.platform

import android.content.Context

/**
 * Holds the application context for platform services that need one
 * (Vibrator, AudioManager). Call [init] once from `Application.onCreate`.
 */
object AndroidPlatform {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun requireContext(): Context =
        appContext ?: error("AndroidPlatform.init(context) must be called from Application.onCreate()")
}
