package com.rexosphere.haptictotp.data

import com.rexosphere.haptictotp.core.HmacAlgorithm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OtpAuthUriTest {
    @Test
    fun parsesFullUri() {
        val a = OtpAuthUri.parse(
            "otpauth://totp/Example%20Corp:alice%40example.com?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ&issuer=Example%20Corp&algorithm=SHA256&period=60&digits=6&pattern=12",
            idGenerator = { "id1" },
        )
        assertEquals("id1", a.id)
        assertEquals("alice@example.com", a.label)
        assertEquals("Example Corp", a.issuer)
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", a.secretBase32)
        assertEquals(HmacAlgorithm.SHA256, a.algorithm)
        assertEquals(60, a.stepSeconds)
        assertEquals(12, a.patternLength)
    }

    @Test
    fun defaultsWhenParametersMissing() {
        val a = OtpAuthUri.parse("otpauth://totp/bob?secret=JBSWY3DPEHPK3PXP")
        assertEquals("bob", a.label)
        assertEquals(null, a.issuer)
        assertEquals(HmacAlgorithm.SHA1, a.algorithm)
        assertEquals(60, a.stepSeconds)
        assertEquals(8, a.patternLength)
    }

    @Test
    fun issuerFromLabelWhenNoParameter() {
        val a = OtpAuthUri.parse("otpauth://totp/GitHub:octocat?secret=JBSWY3DPEHPK3PXP")
        assertEquals("GitHub", a.issuer)
        assertEquals("octocat", a.label)
    }

    @Test
    fun rejectsBadInput() {
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://hotp/x?secret=JBSWY3DPEHPK3PXP") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://totp/x") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://totp/x?secret=not!base32") }
    }

    @Test
    fun percentDecoding() {
        assertEquals("a b@c", OtpAuthUri.percentDecode("a+b%40c"))
        assertEquals("100%", OtpAuthUri.percentDecode("100%"))
    }
}
