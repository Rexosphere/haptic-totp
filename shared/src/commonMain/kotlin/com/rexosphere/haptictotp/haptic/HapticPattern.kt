package com.rexosphere.haptictotp.haptic

/** An ordered sequence of [HapticSymbol]s. This is the "one-time code" of the system. */
data class HapticPattern(val symbols: List<HapticSymbol>) {

    val length: Int get() = symbols.size

    /** ASCII form, e.g. `.-..-.-.`. */
    fun toNotation(): String = symbols.joinToString("") { it.notation.toString() }

    /** Same as [toNotation] but with a space between groups, e.g. `.-.. -.-.`. */
    fun toGroupedNotation(groupSize: Int): String =
        groups(groupSize).joinToString(" ") { HapticPattern(it).toNotation() }

    /** Split into chunks for display/playback. Chunking makes patterns easier to remember. */
    fun groups(groupSize: Int): List<List<HapticSymbol>> {
        require(groupSize > 0) { "groupSize must be positive" }
        return symbols.chunked(groupSize)
    }

    /** Human readable form for screen readers, e.g. "short, long, short". */
    fun toSpokenText(): String = symbols.joinToString(", ") { it.spokenName }

    companion object {
        val EMPTY = HapticPattern(emptyList())

        fun fromNotation(notation: String): HapticPattern =
            HapticPattern(notation.filter { !it.isWhitespace() }.map { HapticSymbol.fromNotation(it) })
    }
}
