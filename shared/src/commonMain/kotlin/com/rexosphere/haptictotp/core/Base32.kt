package com.rexosphere.haptictotp.core

/**
 * RFC 4648 Base32 codec. TOTP secrets are conventionally shared as Base32 text
 * (this is what `otpauth://` URIs and QR codes carry).
 *
 * Decoding is lenient: it ignores case, whitespace, dashes and `=` padding so
 * a user can type a secret exactly as a website printed it.
 */
object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(input: String): ByteArray {
        val cleaned = input.uppercase().filter { it != '=' && !it.isWhitespace() && it != '-' }
        require(cleaned.isNotEmpty()) { "Secret is empty" }
        require(cleaned.all { it in ALPHABET }) { "Secret contains a character that is not valid Base32" }

        val out = ByteArray(cleaned.length * 5 / 8)
        var buffer = 0
        var bitsInBuffer = 0
        var index = 0
        for (c in cleaned) {
            buffer = (buffer shl 5) or ALPHABET.indexOf(c)
            bitsInBuffer += 5
            if (bitsInBuffer >= 8) {
                out[index++] = ((buffer shr (bitsInBuffer - 8)) and 0xFF).toByte()
                bitsInBuffer -= 8
                buffer = buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        return out
    }

    fun encode(bytes: ByteArray, padding: Boolean = false): String {
        val sb = StringBuilder((bytes.size * 8 + 4) / 5)
        var buffer = 0
        var bitsInBuffer = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsInBuffer += 8
            while (bitsInBuffer >= 5) {
                sb.append(ALPHABET[(buffer shr (bitsInBuffer - 5)) and 0x1F])
                bitsInBuffer -= 5
                buffer = buffer and ((1 shl bitsInBuffer) - 1)
            }
        }
        if (bitsInBuffer > 0) {
            sb.append(ALPHABET[(buffer shl (5 - bitsInBuffer)) and 0x1F])
        }
        if (padding) {
            while (sb.length % 8 != 0) sb.append('=')
        }
        return sb.toString()
    }
}
