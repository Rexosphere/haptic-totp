package com.rexosphere.haptictotp.data

import com.rexosphere.haptictotp.core.Base32
import com.rexosphere.haptictotp.core.HmacAlgorithm
import com.rexosphere.haptictotp.core.Totp
import com.rexosphere.haptictotp.core.TotpConfig
import com.rexosphere.haptictotp.haptic.HapticTotp
import com.rexosphere.haptictotp.haptic.PatternConfig

/**
 * One enrolled account. The secret is kept as the Base32 text it was enrolled
 * with; decode on demand with [secretBytes]. Never log or announce it.
 */
data class Account(
    val id: String,
    val label: String,
    val issuer: String?,
    val secretBase32: String,
    val algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    val stepSeconds: Long = TotpConfig.DEFAULT_STEP_SECONDS,
    val patternLength: Int = PatternConfig.DEFAULT_LENGTH,
    val groupSize: Int = PatternConfig.DEFAULT_GROUP_SIZE,
) {
    val displayName: String
        get() = if (issuer.isNullOrBlank()) label else "$issuer ($label)"

    fun secretBytes(): ByteArray = Base32.decode(secretBase32)

    fun patternConfig(): PatternConfig = PatternConfig(length = patternLength, groupSize = groupSize)

    fun hapticTotp(): HapticTotp = HapticTotp(
        secret = secretBytes(),
        totp = Totp(TotpConfig(stepSeconds = stepSeconds, algorithm = algorithm)),
        patternConfig = patternConfig(),
    )
}
