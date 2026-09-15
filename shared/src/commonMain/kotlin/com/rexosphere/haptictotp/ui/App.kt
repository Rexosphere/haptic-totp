package com.rexosphere.haptictotp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.rexosphere.haptictotp.data.AccountRepository
import com.rexosphere.haptictotp.data.DemoData
import com.rexosphere.haptictotp.data.InMemoryAccountRepository
import com.rexosphere.haptictotp.output.AudioOutput
import com.rexosphere.haptictotp.output.HapticOutput
import com.rexosphere.haptictotp.output.createAudioOutput
import com.rexosphere.haptictotp.output.createHapticOutput
import com.rexosphere.haptictotp.ui.screens.AccountListScreen
import com.rexosphere.haptictotp.ui.screens.AddAccountScreen
import com.rexosphere.haptictotp.ui.screens.CodeScreen
import kotlinx.coroutines.launch

/**
 * Root composable shared by Android and iOS. Dependencies are parameters so
 * previews and tests can inject fakes.
 */
@Composable
fun App(
    repository: AccountRepository = remember { InMemoryAccountRepository(listOf(DemoData.demoAccount)) },
    hapticOutput: HapticOutput = remember { createHapticOutput() },
    audioOutput: AudioOutput = remember { createAudioOutput() },
) {
    MaterialTheme {
        Surface {
            var screen by remember { mutableStateOf<Screen>(Screen.AccountList) }
            val accounts by repository.accounts.collectAsState()
            val scope = rememberCoroutineScope()

            when (val current = screen) {
                Screen.AccountList -> AccountListScreen(
                    accounts = accounts,
                    onOpen = { screen = Screen.Code(it.id) },
                    onAdd = { screen = Screen.AddAccount },
                    onDelete = { account -> scope.launch { repository.remove(account.id) } },
                )

                Screen.AddAccount -> AddAccountScreen(
                    onSave = { account ->
                        scope.launch {
                            repository.add(account)
                            screen = Screen.Code(account.id)
                        }
                    },
                    onBack = { screen = Screen.AccountList },
                )

                is Screen.Code -> {
                    val account = repository.get(current.accountId)
                    if (account == null) {
                        screen = Screen.AccountList
                    } else {
                        CodeScreen(
                            account = account,
                            hapticOutput = hapticOutput,
                            audioOutput = audioOutput,
                            onBack = { screen = Screen.AccountList },
                        )
                    }
                }
            }
        }
    }
}
