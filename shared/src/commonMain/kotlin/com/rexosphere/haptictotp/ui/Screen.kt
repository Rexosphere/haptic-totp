package com.rexosphere.haptictotp.ui

/** Minimal navigation state. Swap for a navigation library once the app grows. */
sealed interface Screen {
    data object AccountList : Screen
    data object AddAccount : Screen
    data class Code(val accountId: String) : Screen
}
