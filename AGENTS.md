# AGENTS.md

Instructions for AI coding agents (Claude Code, Copilot, Cursor, Codex, ...)
and for humans who want the short version. Read this before touching code.

## What this project is

A Kotlin Multiplatform (Android + iOS, Compose Multiplatform UI) authenticator
for blind and low-vision users. It generates RFC 6238 TOTP codes with a 60 s
step and presents them as a vibration pattern (and optionally a sound pattern)
made of SHORT and LONG symbols that the user taps back. See `docs/DESIGN.md`
for the design and `README.md` for status and ownership.

## Ground rules

1. **Never change the code encoding casually.** `HapticCodec`, `Hotp`, `Totp`,
   `Base32` and the default `PatternConfig`/`TotpConfig` define the wire
   format shared with servers (`docs/verifier_reference.py`). Any change needs:
   updated unit tests with vectors, the Python reference updated, and a note
   in `docs/DESIGN.md`.
2. **Secrets never leave the process.** Do not log, toast, announce via screen
   reader, or put secrets or the *expected* pattern into `contentDescription`.
   The entered pattern may be announced; the expected one must not be.
3. **Platform code lives in platform source sets.** `commonMain` must not
   import `android.*` or `platform.*`. Use `expect`/`actual` or interfaces
   (`HapticOutput`, `AudioOutput`, `EpochClock`, `AccountRepository`).
4. **Every output channel consumes `List<Segment>`.** Do not re-derive timings
   from the pattern inside a channel; call `pattern.toSegments(timing, groupSize)`
   once and pass it along so vibration and sound stay identical.
5. **Accessibility is a requirement, not a polish step.** Touch targets at
   least 48 dp, every control has a label, results are announced through a
   `liveRegion`, nothing depends on colour alone, and every feature must be
   usable with TalkBack / VoiceOver on.
6. **Do not add dependencies without a reason in the PR description.** The
   current set is Compose Multiplatform, kotlinx-coroutines and KotlinCrypto.
   Do not bump AGP, Kotlin or Gradle casually: they are pinned to what Android
   Studio Narwhal (2025.1) can sync (AGP 8.11.1 is the ceiling for that IDE).
7. **Tests for logic, not for UI.** Anything in `core`, `haptic`, `input`,
   `data` gets a `commonTest`. UI is verified manually on a device for now.

## Repository map

```
shared/src/commonMain/kotlin/com/rexosphere/haptictotp/
  core/      Base32, Hmac (KotlinCrypto wrapper), Hotp, Totp, TotpConfig
  haptic/    HapticSymbol, HapticPattern, PatternConfig, HapticCodec,
             HapticTiming + Segment (+ toSegments / toOnOffTimings), HapticTotp (facade)
  input/     TapClassifier (duration -> symbol), TapEntrySession (collects taps)
  output/    HapticOutput, AudioOutput interfaces + NoOp fallbacks + expect factories
  platform/  EpochClock (injectable wall clock), expect currentEpochMillis()
  data/      Account, OtpAuthUri parser, AccountRepository (+ in-memory impl, DemoData)
  ui/        App (root + nav state), screens/, components/
shared/src/androidMain/   AndroidPlatform (context holder), AndroidHapticOutput, AndroidAudioOutput
shared/src/iosMain/       currentEpochMillis, NoOp channels, MainViewController
shared/src/jvmMain/       JVM stubs so tests run on desktop
shared/src/commonTest/    unit tests
androidApp/               Application + MainActivity, manifest (VIBRATE permission)
iosApp/                   Xcode project; builds the Shared framework through Gradle
docs/                     DESIGN.md, verifier_reference.py
```

## Commands

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"  # macOS, or any JDK 17/21
./gradlew :shared:jvmTest                                  # run all unit tests
./gradlew :shared:jvmTest --tests '*HapticCodecTest*'      # one test class
./gradlew :androidApp:assembleDebug                        # Android build
./gradlew :androidApp:installDebug                         # deploy to connected device
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64      # compile the iOS framework
```

CI (`.github/workflows/ci.yml`) runs the JVM tests, the Android debug build and
the iOS framework link on every push and pull request. Keep it green.

## How to implement an output channel

1. Open the skeleton (`AndroidHapticOutput.kt` or `AndroidAudioOutput.kt`);
   the implementation notes are in the file header.
2. `play(segments)` must suspend until playback finishes or `cancel()` is
   called. Use `delay(segments.totalDurationMillis())`, never block a thread.
3. Set `isAvailable` from real hardware capability once `play` works. The
   Code screen enables its buttons from that flag.
4. Test on a real device. Emulators do not vibrate and have poor audio timing.
5. Keep the timing knobs in `HapticTiming`; if you need a new knob, add it
   there so the sound channel gets it too.

## Conventions

- Kotlin official code style (`.editorconfig`), 4-space indent, trailing commas
  in multi-line parameter lists.
- Platform-specific files are named `Thing.android.kt`, `Thing.ios.kt`, `Thing.jvm.kt`.
- Public API in `core` and `haptic` has KDoc explaining *why*, not just what.
- Branches: `feat/<topic>`, `fix/<topic>`, `docs/<topic>`. Small PRs against `main`.
- Commit messages: imperative subject line, body explains the reason.
- Do not commit `local.properties`, keystores, or anything under `build/`.

## Things that are intentionally not done yet

- Storage is in-memory; accounts vanish on restart. A Keystore/Keychain-backed
  `AccountRepository` is a planned task, not a bug.
- Verification in the app is local, for development. Production verifies on
  the server with the same algorithm (`docs/verifier_reference.py`).
- No QR scanning; paste the `otpauth://` URI instead.
- iOS channels are `NoOp`. The UI reports "not implemented on this platform yet".

## PR checklist

- [ ] `./gradlew :shared:jvmTest` passes locally
- [ ] `./gradlew :androidApp:assembleDebug` passes
- [ ] No new `android.*` / `platform.*` imports in `commonMain`
- [ ] No secret or expected pattern reaches logs or screen-reader text
- [ ] New logic has tests; new timing/encoding changes update `docs/DESIGN.md`
- [ ] Tried once with TalkBack (Android) or VoiceOver (iOS) if UI changed
