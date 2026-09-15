package com.rexosphere.haptictotp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rexosphere.haptictotp.data.Account
import com.rexosphere.haptictotp.haptic.HapticPattern
import com.rexosphere.haptictotp.haptic.HapticSymbol
import com.rexosphere.haptictotp.haptic.HapticTiming
import com.rexosphere.haptictotp.haptic.toSegments
import com.rexosphere.haptictotp.input.TapClassifier
import com.rexosphere.haptictotp.input.TapEntrySession
import com.rexosphere.haptictotp.output.AudioOutput
import com.rexosphere.haptictotp.output.HapticOutput
import com.rexosphere.haptictotp.platform.EpochClock
import com.rexosphere.haptictotp.platform.SystemEpochClock
import com.rexosphere.haptictotp.ui.components.PatternVisual
import com.rexosphere.haptictotp.ui.components.TapPad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The main screen: receive the current code (feel / hear), then tap it back.
 * Verification here is local; a real deployment verifies on the server (see docs/DESIGN.md).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(
    account: Account,
    hapticOutput: HapticOutput,
    audioOutput: AudioOutput,
    onBack: () -> Unit,
    clock: EpochClock = SystemEpochClock,
    timing: HapticTiming = HapticTiming(),
) {
    val scope = rememberCoroutineScope()
    val hapticTotp = remember(account) { account.hapticTotp() }
    val classifier = remember { TapClassifier() }

    var nowSeconds by remember { mutableStateOf(clock.nowEpochSeconds()) }
    LaunchedEffect(account.id) {
        while (true) {
            nowSeconds = clock.nowEpochSeconds()
            delay(250)
        }
    }

    val counter = hapticTotp.totp.counterAt(nowSeconds)
    val pattern = remember(counter, account.id) { hapticTotp.patternForCounter(counter) }
    val remaining = hapticTotp.secondsRemaining(nowSeconds)
    val progress = remaining.toFloat() / account.stepSeconds.toFloat()
    val segments = remember(pattern, timing) { pattern.toSegments(timing, account.groupSize) }

    val session = remember(account.id) { TapEntrySession(account.patternLength, classifier) }
    var entered by remember { mutableStateOf<List<HapticSymbol>>(emptyList()) }
    var result by remember { mutableStateOf<Boolean?>(null) }
    var playingHaptic by remember { mutableStateOf(false) }
    var playingAudio by remember { mutableStateOf(false) }
    var showDigits by remember { mutableStateOf(false) }

    fun addSymbol(durationMillis: Long) {
        if (session.press(durationMillis) == null) return
        entered = session.symbols
        if (session.isComplete) {
            result = hapticTotp.verify(session.pattern, clock.nowEpochSeconds())
        }
    }

    fun reset() {
        session.clear()
        entered = emptyList()
        result = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account.displayName) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Current code", style = MaterialTheme.typography.titleMedium)
            PatternVisual(pattern = pattern, groupSize = account.groupSize, modifier = Modifier.fillMaxWidth())
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text(
                "$remaining s left in this ${account.stepSeconds} s window",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (showDigits) {
                Text(
                    "Numeric equivalent: ${hapticTotp.digitsAt(nowSeconds)}   pattern: ${pattern.toGroupedNotation(account.groupSize)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            playingHaptic = true
                            try { hapticOutput.play(segments) } finally { playingHaptic = false }
                        }
                    },
                    enabled = hapticOutput.isAvailable && !playingHaptic,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text(if (playingHaptic) "Vibrating…" else "Feel it") }

                Button(
                    onClick = {
                        scope.launch {
                            playingAudio = true
                            try { audioOutput.play(segments) } finally { playingAudio = false }
                        }
                    },
                    enabled = audioOutput.isAvailable && !playingAudio,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text(if (playingAudio) "Playing…" else "Hear it") }
            }
            Text(
                "Vibration: ${if (hapticOutput.isAvailable) "ready" else "not implemented on this platform yet"}. " +
                    "Sound: ${if (audioOutput.isAvailable) "ready" else "not implemented on this platform yet"}.",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(8.dp))
            Text("Tap it back", style = MaterialTheme.typography.titleMedium)
            PatternVisual(
                pattern = HapticPattern(entered),
                groupSize = account.groupSize,
                placeholderSlots = account.patternLength,
                revealToScreenReader = true,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
            TapPad(enabled = !session.isComplete, onPress = ::addSymbol)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { addSymbol(0) },
                    enabled = !session.isComplete,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Short") }
                OutlinedButton(
                    onClick = { addSymbol(classifier.longPressThresholdMillis) },
                    enabled = !session.isComplete,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Long") }
                OutlinedButton(
                    onClick = { session.undo(); entered = session.symbols; result = null },
                    enabled = entered.isNotEmpty() && !session.isComplete,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Undo") }
            }

            val resultText = when (result) {
                null -> "${entered.size} of ${account.patternLength} symbols entered"
                true -> "Matched. Code accepted."
                false -> "Not matched. Try again."
            }
            Text(
                resultText,
                style = MaterialTheme.typography.titleLarge,
                color = when (result) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Button(onClick = ::reset, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Clear") }

            TextButton(onClick = { showDigits = !showDigits }) {
                Text(if (showDigits) "Hide developer info" else "Show developer info")
            }
        }
    }
}
