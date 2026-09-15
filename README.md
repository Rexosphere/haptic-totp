# Haptic TOTP

A multi-factor authenticator for blind and low-vision (BLV) users. Instead of
showing a 6-digit number, the app turns the time-based one-time code into a
short **vibration pattern** (and optionally a **sound pattern**). The user feels
it, then taps it back: short tap = short buzz, long press = long buzz.

The crypto is standard RFC 6238 TOTP with a **60 second** step. Only the last
mile changes: `HOTP value mod 2^N` becomes a pattern of N short/long symbols
instead of `mod 10^6` becoming digits. The idea follows the
[Haptic2FA](https://dl.acm.org/doi/10.1145/3676509) study.

Full design and roadmap: [docs/DESIGN.md](docs/DESIGN.md).
Guide for contributors and AI coding agents: [AGENTS.md](AGENTS.md).

## Status

| Piece | State | Owner |
|---|---|---|
| TOTP / HOTP / Base32 core, RFC test vectors | done | base |
| Haptic codec, timing, tap classifier, verification | done | base |
| Compose Multiplatform UI (accounts, feel / hear, tap back) | done, basic | base |
| Android vibration channel (`AndroidHapticOutput`) | skeleton | charindith |
| Android sound channel (`AndroidAudioOutput`) | skeleton | open |
| iOS vibration + sound | not started | open |
| Persistent, encrypted account storage | not started | open |
| QR code enrolment | not started | open |
| Server-side verifier + demo login page | reference in Python | open |

## Repository layout

```
shared/        Kotlin Multiplatform library: all logic + Compose UI (Android, iOS, JVM)
  commonMain/  core (TOTP), haptic (pattern codec + timing), input (tap entry),
               output (HapticOutput / AudioOutput interfaces), data, ui
  androidMain/ Android implementations of the output channels
  iosMain/     iOS implementations (stubs) + Compose entry point for Swift
  jvmMain/     JVM stubs so unit tests run on a desktop JVM
  commonTest/  Unit tests (RFC 4226 / 6238 vectors, codec, timing, parsing)
androidApp/    Android application shell
iosApp/        Xcode project that embeds the shared framework
docs/          DESIGN.md (plan), verifier_reference.py (server-side reference)
```

## Building

Requirements: Android Studio Narwhal (2025.1) or newer, JDK 17 or 21, Android SDK (platform 36), Xcode 16+ for iOS.
Versions are pinned in `gradle/libs.versions.toml` (AGP 8.11.2, Kotlin 2.2.20, Compose Multiplatform 1.9.0, Gradle 8.14.3); do not bump AGP past what the oldest Android Studio on the team supports.
Android Studio's bundled JBR works; on macOS:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

```sh
./gradlew :shared:jvmTest                 # unit tests (fast, no emulator)
./gradlew :androidApp:assembleDebug       # Android APK
./gradlew :androidApp:installDebug        # install on a connected device
open iosApp/iosApp.xcodeproj              # iOS: build the iosApp scheme in Xcode
```

Or open the root folder in Android Studio, wait for Gradle sync, pick the `androidApp` run configuration and press Run.

## Trying it

The app ships with a demo account using the RFC 6238 test secret
(`GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ`). Open it, tap **Show developer info** to
see the pattern in dot/dash notation and the classic numeric code for the same
moment. Any other authenticator configured with that secret and a 60 s period
shows the same digits, and `python3 docs/verifier_reference.py <secret>` prints
the same pattern.

## Work split

1. **Vibration** (charindith): implement `AndroidHapticOutput.play()` in
   `shared/src/androidMain/.../output/AndroidHapticOutput.kt`. The timeline is
   already computed; see the notes in that file and in `docs/DESIGN.md`.
2. **Sound**: implement `AndroidAudioOutput.play()` next to it, same contract.
3. **Everything else** is listed as open in the status table above and in the
   roadmap in `docs/DESIGN.md`. Claim a task by opening an issue.
