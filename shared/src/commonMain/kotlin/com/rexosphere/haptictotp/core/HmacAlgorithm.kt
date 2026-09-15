package com.rexosphere.haptictotp.core

/** Hash functions permitted by RFC 6238. SHA1 is the de-facto default everywhere. */
enum class HmacAlgorithm(val otpauthName: String) {
    SHA1("SHA1"),
    SHA256("SHA256"),
    SHA512("SHA512");

    companion object {
        fun fromOtpauthName(name: String?): HmacAlgorithm =
            entries.firstOrNull { it.otpauthName.equals(name, ignoreCase = true) } ?: SHA1
    }
}
