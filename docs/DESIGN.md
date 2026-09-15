# Haptic TOTP: design and plan

## 1. Goal

Give blind and low-vision (BLV) users a second factor they can use privately,
on one device, without a screen reader speaking a code out loud. The
authenticator produces a **short vibration pattern** derived from a shared
secret and the current time, exactly like TOTP, and the user proves they
received it by **tapping it back**.

Reference: Haptic2FA (Bhole et al., PACM HCI 2024,
<https://dl.acm.org/doi/10.1145/3676509>) showed that BLV users can reliably
perceive and reproduce Morse-style vibration codes, and compared three entry
methods: picking from options ("Buttons"), pressing Dot/Dash buttons
("Dot-Dash") and short/long presses on one area ("Gesture"). We implement
Gesture as the primary method and Dot-Dash as the screen-reader fallback.

## 2. Non-goals (for now)

- Replacing the server side of existing TOTP deployments. A service has to
  opt in by running our verifier (Section 6).
- Watch support. The timing model already fits a watch; it is just not wired.
- Push-based approval (that is a different product).

## 3. Threat model

| Threat | Mitigation |
|---|---|
| Bystander overhears the code | Vibration is silent. Sound is opt-in, headphone-recommended. Screen reader never announces the expected pattern. |
| Attacker guesses the code | Small code space (Section 5), so the **verifier must rate-limit**: 3 attempts per user per window, lockout after repeated failures. |
| Replay of a captured code | Server tracks the last accepted counter per user and rejects reuse (RFC 6238 §5.2). |
| Clock drift | Accept current step ±1 (configurable). 60 s step also makes drift less damaging. |
| Secret theft from the device | Planned: encrypted storage (Android Keystore / iOS Keychain). Currently in-memory only. |
| Phishing | Same exposure as classic TOTP; out of scope. |

## 4. How a code is made

```
secret (Base32 from otpauth:// URI)
   │
   ▼
counter = floor((now - T0) / 60)                        RFC 6238, step = 60 s
   │
   ▼
HMAC-SHA1(secret, counter as 8-byte big-endian)          RFC 4226
   │
   ▼
dynamic truncation -> 31-bit integer V                   RFC 4226 §5.3
   │
   ▼
pattern = low N bits of V, most-significant first        HapticCodec
          1 -> LONG ("-"), 0 -> SHORT (".")               N = 8 by default
   │
   ▼
".-.. -.-."   grouped in 4s for playback and display
```

Everything above the last two boxes is unmodified TOTP, so the classic
6-digit code for the same moment is still available (`HapticTotp.digitsAt`)
for debugging against any other authenticator.

Verification accepts the pattern for counters `c-1, c, c+1`.

### Golden values (RFC secret `12345678901234567890`)

| counter | HOTP value | pattern (N=8) |
|---|---|---|
| 0 | 1284755224 | `...--...` |
| 1 | 1094287082 | `---.-.-.` |

These are asserted in `HapticCodecTest` and in `docs/verifier_reference.py`.

## 5. Pattern and timing

Symbols: SHORT and LONG. Timing (Morse-like, from Haptic2FA), all multiples
of one **unit** (default 200 ms):

| element | length |
|---|---|
| SHORT (dot) | 1 unit |
| LONG (dash) | 3 units |
| gap between symbols | 1 unit |
| gap between groups | 3 units |
| lead-in silence | 300 ms |

An 8-symbol code therefore takes between 3.7 s (all short) and 6.9 s (all
long) to play. The same `List<Segment>` timeline drives vibration, sound and
the visual dots, so channels never disagree.

### Entropy and brute force

With two symbols each symbol carries one bit:

| symbols | codes | chance per guess | notes |
|---|---|---|---|
| 6 | 64 | 1.6 % | too weak |
| **8 (default)** | 256 | 0.39 % | two groups of 4, comfortable to remember |
| 10 | 1024 | 0.10 % | |
| 12 | 4096 | 0.024 % | three groups of 4 |
| 6-digit TOTP | 1,000,000 | 0.0001 % | for comparison |

A 6-digit code is ~12 bits stronger than our default. The gap is closed by
the verifier, not the user:

- lock the account for at least 30 minutes after 3 consecutive failures.
  That caps an online attacker at ~6 attempts per hour, i.e. ~2.3 % per hour
  for N = 8 and ~0.6 % for N = 10 (P ≈ attempts / 2^N). A softer policy such
  as 5 failures then a 10 minute lock allows ~30 attempts per hour (~12 % for
  N = 8) and is not acceptable;
- note that NIST SP 800-63B §5.1.4.1 describes OTP outputs as truncated to
  "as few as 6 decimal digits (approximately 20 bits of entropy)" and §5.1.4.2
  requires rate limiting for any output below 64 bits; an 8-symbol haptic code
  carries 8 bits, which is why it is a second factor only. The same section
  requires a clock-based nonce to change at least every 2 minutes, which our
  60 s step satisfies;
- treat haptic codes as a second factor only, never as a sole factor;
- let a service request a longer code with the `pattern=12` URI parameter.

Alternatives we may test later: a third symbol (double-short) for 1.58 bits
per symbol, or intensity as an extra dimension on phones with amplitude
control. Both cost learnability, which is why they are not the default.

## 6. System architecture

```
┌────────────────────────────┐        ┌────────────────────────────┐
│ Phone app (this repo)      │        │ Service (website / API)    │
│                            │        │                            │
│ shared/ (KMP)              │        │ verifier_reference.py or   │
│  core   TOTP, HOTP, Base32 │        │ any port of it             │
│  haptic codec + timing     │        │                            │
│  input  tap classification │        │ enrolment: issues          │
│  output HapticOutput       │        │  otpauth://totp/...?period=60│
│         AudioOutput        │        │ login: user taps pattern   │
│  ui     Compose screens    │        │  into the login page OR    │
│                            │        │  the app posts it (future) │
│ androidApp/  iosApp/       │        └────────────────────────────┘
└────────────────────────────┘
```

Where does the tapped pattern go? Two modes:

1. **Local check (built today).** The app verifies the tap-back itself. This
   is what we need for developing the channels and for user studies: the
   measured quantity is "can the user reproduce what they felt".
2. **Remote check (planned).** The login page shows Short / Long buttons (or
   a single press area) and posts the pattern to the server. The app is only
   the generator, just like a classic authenticator. Same codec, same tests.

## 7. User flow

1. **Enrol.** Scan a QR code or paste the `otpauth://` URI. Secret is stored
   (later: encrypted). Confirmation is a distinct vibration.
2. **Receive.** Open the account. The app plays the current pattern
   immediately (setting) or on "Feel it". "Replay" is always available and is
   free: replaying does not weaken security.
3. **Enter.** Tap the pattern back on the pad (short tap / long press), or use
   the Short / Long buttons when a screen reader is active. Each accepted tap
   gives a tiny confirmation tick so the user can count.
4. **Result.** Distinct success and failure vibrations; the text result is a
   live region so screen readers announce it. On failure: "Replay" then retry.
5. **Timing.** The bar shows time left in the 60 s window. If fewer than
   ~10 s remain the app should offer to wait for the next code rather than
   start entry that will straddle the boundary (the ±1 step tolerance covers
   this anyway, but the UX is cleaner).

## 8. Accessibility requirements

- Works fully with TalkBack and VoiceOver; the tap pad has a Dot-Dash button
  fallback because explore-by-touch intercepts raw presses.
- Never voice the expected pattern; voicing the *entered* pattern is fine.
- All controls at least 48 dp, high contrast, no colour-only meaning.
- Playback speed (`unitMillis`), long-press threshold and the group size are
  user settings, because motor and perception abilities vary widely.
- Confirmation haptics on every tap, distinct from the code itself (e.g. a
  very short 30 ms tick at low amplitude).

## 9. Roadmap

| # | Milestone | Deliverable | Owner |
|---|---|---|---|
| M0 | Base app | KMP project, core + codec + tests, UI, CI, docs | done |
| M1 | Vibration channel | `AndroidHapticOutput` with waveform + primitives, confirmation ticks | charindith |
| M2 | Sound channel | `AndroidAudioOutput`, opt-in switch, headphone hint | open |
| M3 | Entry tuning | adaptive long-press threshold, per-tap feedback, settings screen | open |
| M4 | Storage + enrolment | encrypted repository, QR scanner | open |
| M5 | Server side | Node/Python verifier with rate limiting, demo login page | open |
| M6 | iOS channels | CoreHaptics + AVAudioEngine implementations | open |
| M7 | Evaluation | study with BLV participants: error rate, time, preference vs digits | all |

## 10. Open questions

- Default length 8 vs 10: pick after the first round of user testing.
- Should a service be allowed to request N > 12? The codec supports 31, the
  human probably does not.
- Amplitude as a third dimension: worth it, or does it hurt learnability?
- Watch-first flow (pattern on wrist, tap on phone) for users who keep the
  phone in a pocket.
