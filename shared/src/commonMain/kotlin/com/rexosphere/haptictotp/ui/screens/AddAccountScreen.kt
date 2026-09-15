package com.rexosphere.haptictotp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rexosphere.haptictotp.core.Base32
import com.rexosphere.haptictotp.core.TotpConfig
import com.rexosphere.haptictotp.data.Account
import com.rexosphere.haptictotp.data.OtpAuthUri
import com.rexosphere.haptictotp.haptic.PatternConfig

/**
 * Manual enrolment. QR scanning is a follow-up task; pasting the otpauth URI
 * the QR code contains already works.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onSave: (Account) -> Unit,
    onBack: () -> Unit,
) {
    var uri by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = null
        runCatching {
            if (uri.isNotBlank()) {
                OtpAuthUri.parse(uri)
            } else {
                require(label.isNotBlank()) { "Account name is required" }
                Base32.decode(secret)
                Account(
                    id = OtpAuthUri.newAccountId(),
                    label = label.trim(),
                    issuer = issuer.trim().ifBlank { null },
                    secretBase32 = secret.trim(),
                    stepSeconds = TotpConfig.DEFAULT_STEP_SECONDS,
                    patternLength = PatternConfig.DEFAULT_LENGTH,
                )
            }
        }.onSuccess(onSave).onFailure { error = it.message ?: "Could not add account" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add account") },
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
            Text("Paste an otpauth:// URI", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uri,
                onValueChange = { uri = it },
                label = { Text("otpauth://totp/...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            HorizontalDivider()

            Text("Or enter the details manually", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuer (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Account name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Secret (Base32)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(onClick = ::submit, modifier = Modifier.fillMaxWidth()) {
                Text("Save account")
            }
        }
    }
}
