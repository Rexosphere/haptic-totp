package com.rexosphere.haptictotp.data

import com.rexosphere.haptictotp.core.Base32
import com.rexosphere.haptictotp.core.HmacAlgorithm
import com.rexosphere.haptictotp.core.TotpConfig
import com.rexosphere.haptictotp.haptic.PatternConfig
import kotlin.random.Random

/**
 * Parses the de-facto standard `otpauth://totp/...` URI that QR codes carry:
 *
 *   otpauth://totp/Issuer:user@example.com?secret=BASE32&issuer=Issuer&algorithm=SHA1&period=60
 *
 * Unknown parameters are ignored. `digits` is ignored because we do not emit digits.
 * The custom `pattern` parameter (symbol count) lets a service opt in to longer codes.
 */
object OtpAuthUri {
    private const val PREFIX = "otpauth://totp/"

    fun isOtpAuthUri(text: String): Boolean = text.trim().lowercase().startsWith("otpauth://")

    fun parse(uri: String, idGenerator: () -> String = ::newAccountId): Account {
        val trimmed = uri.trim()
        require(trimmed.lowercase().startsWith(PREFIX)) { "Only otpauth://totp/ URIs are supported" }

        val rest = trimmed.substring(PREFIX.length)
        val questionMark = rest.indexOf('?')
        val rawLabel = if (questionMark >= 0) rest.substring(0, questionMark) else rest
        val rawQuery = if (questionMark >= 0) rest.substring(questionMark + 1) else ""

        val params = rawQuery.split('&')
            .filter { it.isNotBlank() }
            .associate { pair ->
                val eq = pair.indexOf('=')
                if (eq < 0) percentDecode(pair).lowercase() to ""
                else percentDecode(pair.substring(0, eq)).lowercase() to percentDecode(pair.substring(eq + 1))
            }

        val label = percentDecode(rawLabel)
        val colon = label.indexOf(':')
        val labelIssuer = if (colon >= 0) label.substring(0, colon).trim() else null
        val accountName = if (colon >= 0) label.substring(colon + 1).trim() else label.trim()

        val secret = params["secret"] ?: throw IllegalArgumentException("URI has no secret parameter")
        Base32.decode(secret) // validate early, throws IllegalArgumentException

        val period = params["period"]?.toLongOrNull() ?: TotpConfig.DEFAULT_STEP_SECONDS
        val patternLength = params["pattern"]?.toIntOrNull() ?: PatternConfig.DEFAULT_LENGTH

        return Account(
            id = idGenerator(),
            label = accountName.ifBlank { "Unnamed" },
            issuer = params["issuer"]?.takeIf { it.isNotBlank() } ?: labelIssuer,
            secretBase32 = secret,
            algorithm = HmacAlgorithm.fromOtpauthName(params["algorithm"]),
            stepSeconds = period,
            patternLength = patternLength,
        )
    }

    fun percentDecode(s: String): String {
        val sb = StringBuilder(s.length)
        val bytes = ArrayList<Byte>()
        var i = 0
        fun flushBytes() {
            if (bytes.isNotEmpty()) {
                sb.append(bytes.toByteArray().decodeToString())
                bytes.clear()
            }
        }
        while (i < s.length) {
            val c = s[i]
            when {
                c == '%' && i + 2 < s.length + 0 && i + 2 <= s.length - 1 -> {
                    val hex = s.substring(i + 1, i + 3)
                    val value = hex.toIntOrNull(16)
                    if (value != null) {
                        bytes += value.toByte()
                        i += 3
                        continue
                    }
                    flushBytes(); sb.append(c); i++
                }
                c == '+' -> { flushBytes(); sb.append(' '); i++ }
                else -> { flushBytes(); sb.append(c); i++ }
            }
        }
        flushBytes()
        return sb.toString()
    }

    fun newAccountId(): String = Random.nextLong().toULong().toString(16)
}
