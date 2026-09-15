package com.rexosphere.haptictotp.core

import org.kotlincrypto.macs.hmac.sha1.HmacSHA1
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA512

/** Thin wrapper over KotlinCrypto so the rest of the code never touches the library directly. */
object Hmac {
    fun compute(algorithm: HmacAlgorithm, key: ByteArray, message: ByteArray): ByteArray =
        when (algorithm) {
            HmacAlgorithm.SHA1 -> HmacSHA1(key).doFinal(message)
            HmacAlgorithm.SHA256 -> HmacSHA256(key).doFinal(message)
            HmacAlgorithm.SHA512 -> HmacSHA512(key).doFinal(message)
        }
}
