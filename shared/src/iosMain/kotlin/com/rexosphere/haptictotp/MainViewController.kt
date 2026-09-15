package com.rexosphere.haptictotp

import androidx.compose.ui.window.ComposeUIViewController
import com.rexosphere.haptictotp.ui.App

@Suppress("unused", "FunctionName") // Called from Swift (iosApp/iosApp/ContentView.swift)
fun MainViewController() = ComposeUIViewController { App() }
