package com.rexosphere.haptictotp.haptic

/**
 * Morse-style timing shared by every output channel (vibration, sound, visual).
 *
 * Defaults follow the Haptic2FA study: dot = 1 unit, dash = 3 units, gap
 * between symbols = 1 unit. We add a longer gap between groups so the user can
 * feel the chunking. Tune [unitMillis] per device; 150-250 ms is the useful range.
 */
data class HapticTiming(
    val unitMillis: Long = 200,
    val shortUnits: Int = 1,
    val longUnits: Int = 3,
    val gapUnits: Int = 1,
    val groupGapUnits: Int = 3,
    /** Silence before the first symbol so the user is ready. */
    val leadInMillis: Long = 300,
) {
    val shortMillis: Long get() = unitMillis * shortUnits
    val longMillis: Long get() = unitMillis * longUnits
    val gapMillis: Long get() = unitMillis * gapUnits
    val groupGapMillis: Long get() = unitMillis * groupGapUnits

    fun durationOf(symbol: HapticSymbol): Long = when (symbol) {
        HapticSymbol.SHORT -> shortMillis
        HapticSymbol.LONG -> longMillis
    }
}

/**
 * One slice of a playback timeline. [on] = the motor/speaker is active.
 * Every output implementation (vibration, sound) consumes a `List<Segment>`
 * so the channels stay perfectly in sync with each other.
 */
data class Segment(val on: Boolean, val durationMillis: Long)

/** Flatten a pattern into an on/off timeline. */
fun HapticPattern.toSegments(
    timing: HapticTiming = HapticTiming(),
    groupSize: Int = PatternConfig.DEFAULT_GROUP_SIZE,
): List<Segment> {
    val out = ArrayList<Segment>(symbols.size * 2 + 2)
    if (timing.leadInMillis > 0) out += Segment(on = false, durationMillis = timing.leadInMillis)
    val groups = groups(groupSize)
    groups.forEachIndexed { gi, group ->
        group.forEachIndexed { si, symbol ->
            out += Segment(on = true, durationMillis = timing.durationOf(symbol))
            val isLastInGroup = si == group.lastIndex
            val isLastGroup = gi == groups.lastIndex
            when {
                !isLastInGroup -> out += Segment(on = false, durationMillis = timing.gapMillis)
                !isLastGroup -> out += Segment(on = false, durationMillis = timing.groupGapMillis)
            }
        }
    }
    return out
}

fun List<Segment>.totalDurationMillis(): Long = sumOf { it.durationMillis }

/**
 * Android `VibrationEffect.createWaveform(timings, -1)` convention: alternating
 * off/on durations starting with an OFF slot. Kept in common code so the sound
 * channel or a test can use the identical timeline.
 */
fun List<Segment>.toOnOffTimings(): LongArray {
    val timings = ArrayList<Long>(size + 1)
    var expectOn = false
    for (segment in this) {
        if (segment.on != expectOn) timings += 0L // insert an empty slot to keep the alternation
        timings += segment.durationMillis
        expectOn = !segment.on
    }
    return timings.toLongArray()
}
