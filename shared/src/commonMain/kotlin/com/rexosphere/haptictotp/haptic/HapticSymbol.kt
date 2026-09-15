package com.rexosphere.haptictotp.haptic

/**
 * The alphabet a user has to feel and reproduce. Two symbols mirrors Morse code
 * and the Haptic2FA study: a short buzz ("dot") and a long buzz ("dash").
 *
 * [notation] is the ASCII form used in logs, tests and the debug UI.
 */
enum class HapticSymbol(val notation: Char, val spokenName: String) {
    SHORT('.', "short"),
    LONG('-', "long");

    companion object {
        fun fromNotation(c: Char): HapticSymbol =
            entries.firstOrNull { it.notation == c }
                ?: throw IllegalArgumentException("Unknown haptic symbol '$c'")
    }
}
