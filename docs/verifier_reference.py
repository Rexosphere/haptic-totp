#!/usr/bin/env python3
"""
Reference verifier for Haptic TOTP, in plain Python (stdlib only).

Use this on a server, in tests, or to cross-check the Kotlin implementation.
The encoding is identical to `HapticCodec` in the Kotlin code:

    pattern = HOTP(secret, floor(now / 60)) mod 2^length, most-significant bit first
    1 -> "-" (long)   0 -> "." (short)

Usage:
    python3 verifier_reference.py GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ            # print current pattern
    python3 verifier_reference.py GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ ...--...   # verify a tapped pattern
"""
import base64
import hashlib
import hmac
import struct
import sys
import time

STEP_SECONDS = 60
PATTERN_LENGTH = 8
ALLOWED_SKEW_STEPS = 1


def decode_secret(base32_text: str) -> bytes:
    cleaned = "".join(base32_text.split()).replace("-", "").rstrip("=").upper()
    return base64.b32decode(cleaned + "=" * (-len(cleaned) % 8))


def hotp_truncated(secret: bytes, counter: int, algorithm=hashlib.sha1) -> int:
    digest = hmac.new(secret, struct.pack(">Q", counter & 0xFFFFFFFFFFFFFFFF), algorithm).digest()
    offset = digest[-1] & 0x0F
    return struct.unpack(">I", digest[offset:offset + 4])[0] & 0x7FFFFFFF


def pattern_for_counter(secret: bytes, counter: int, length: int = PATTERN_LENGTH) -> str:
    value = hotp_truncated(secret, counter)
    return "".join("-" if (value >> bit) & 1 else "." for bit in range(length - 1, -1, -1))


def pattern_at(secret: bytes, epoch_seconds: int, step: int = STEP_SECONDS) -> str:
    return pattern_for_counter(secret, epoch_seconds // step)


def verify(secret: bytes, candidate: str, epoch_seconds: int, step: int = STEP_SECONDS,
           skew: int = ALLOWED_SKEW_STEPS) -> bool:
    candidate = "".join(candidate.split())
    counter = epoch_seconds // step
    return any(hmac.compare_digest(pattern_for_counter(secret, counter + s), candidate)
               for s in range(-skew, skew + 1))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    secret = decode_secret(sys.argv[1])
    now = int(time.time())
    if len(sys.argv) == 2:
        print(pattern_at(secret, now), f"({STEP_SECONDS - now % STEP_SECONDS}s left)")
    else:
        print("MATCH" if verify(secret, sys.argv[2], now) else "NO MATCH")
