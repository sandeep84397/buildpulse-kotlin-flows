# BuildPulse: Learn Kotlin asynchronous streams by building one system

BuildPulse is the code companion for a nine-part learning series about Kotlin coroutines and asynchronous streams.

Instead of presenting unrelated snippets, the series evolves one Android/Jetpack Compose CI/CD monitor. Every article begins with an engineering limitation, introduces only the concept needed to solve it, and leaves behind a runnable checkpoint.

## Current milestone

**Article 03 — `callbackFlow` and listener cleanup**

The app now connects two problems:

> A real CI SDK pushes future updates through listeners. `callbackFlow` turns each listener lifetime into a cold, cancellable Flow execution.

Article 03 introduces only the callback bridge: `addListener()`, `trySend()`, and `awaitClose { removeListener() }`. `channelFlow`, buffering strategy, hot streams, sharing, and `Channel` remain absent until their own articles.

- [Read Article 03: callbackFlow](articles/03-callback-flow.md)
- [Read Article 02: Cold Flow](articles/02-cold-flow.md)
- [Start with Article 01](articles/01-asynchronous-streams.md)
- [Inspect the callbackFlow repository](app/src/main/java/io/github/buildpulse/simulation/CallbackFlowBuildHistoryRepository.kt)
- [Inspect the callback cleanup tests](app/src/test/java/io/github/buildpulse/simulation/CallbackFlowBuildHistoryRepositoryTest.kt)
- Git tag: `article-03-callback-flow`

## The experiment

BuildPulse simulates this pipeline:

```text
QUEUED → COMPILING → RUNNING_TESTS → SECURITY_SCAN → DEPLOYING → SUCCEEDED
```

The first lab still shows two values:

- **Server now:** the current simulated pipeline stage.
- **UI snapshot:** the last value returned by `fetchStatus()`.

Run this sequence:

1. Tap **Fetch snapshot**.
2. Tap **Advance server**.
3. Observe that the server changes while the fetched snapshot stays unchanged.
4. Fetch again to make the values match.

That mismatch is not a UI bug. It is the original modeling problem.

![One result compared with a changing build](diagrams/article-01-one-result-vs-stream.svg)

The Article 03 lab exposes one callback-based CI SDK and two collectors:

1. Before either collector starts, the SDK has zero listeners; earlier callbacks are missed.
2. Start Collector A. Its cold `callbackFlow` execution registers one listener.
3. Start Collector B. A second execution registers a second listener.
4. Advance the server. The SDK calls both listeners; both collectors receive the future update.
5. Stop A. `awaitClose` removes A's listener while B continues.

![callbackFlow registers, forwards, and unregisters a listener](diagrams/article-03-callback-flow.gif)

## Run the Android app

Requirements:

- JDK 17
- Android SDK Platform 36
- Android Studio with AGP 9.2 support, or the included Gradle wrapper

```bash
git clone https://github.com/sandeep84397/buildpulse-kotlin-flows.git
cd buildpulse-kotlin-flows
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Open the project in Android Studio and run the `app` configuration on an emulator or device running Android 8.0 or newer.

## Learning roadmap

| Article | BuildPulse limitation | Concept introduced | Milestone |
|---|---|---|---|
| 01 | One fetched status becomes stale | Asynchronous-stream mental model | `article-01-introduction` |
| 02 | Repeated snapshots lack structured collection | Cold `Flow` | `article-02-cold-flow` |
| 03 | CI SDK exposes listeners | `callbackFlow` | `article-03-callback-flow` |
| 04 | Compiler, tests, and scanner emit concurrently | `channelFlow` | Unpublished |
| 05 | Multiple screens repeat one upstream operation | Hot streams | Unpublished |
| 06 | Current state and transient events need different rules | `StateFlow` vs `SharedFlow` | Unpublished |
| 07 | Cold upstream work must become shared | `stateIn` vs `shareIn` | Unpublished |
| 08 | Build jobs must be distributed to workers | `Channel` | Unpublished |
| 09 | Developers need one selection framework | Final decision guide | Unpublished |

## Repository learning contract

```text
1 published article = 1 instructional commit = 1 tag = 1 GitHub Release
```

- `main` contains only concepts explained by published articles.
- Each milestone is cumulative and runnable.
- Each article links to its exact tag and comparison with the previous tag.
- Later code is never placed ahead of the written explanation.
- The Medium article remains the primary learning product; this repository is its optional executable proof.

## Project structure

```text
app/src/main/java/io/github/buildpulse/
├── model/          Immutable build stages and snapshots
├── simulation/     One-shot status, callback SDK, and Flow adapters
├── dashboard/      Tested UI state plus snapshot and callback-lifetime labs
└── MainActivity.kt Dependency assembly and app entry point

articles/           Self-contained article companions
diagrams/           Editable source diagrams
docs/               Approved specs and implementation plans
```

## Technology choices

- Kotlin 2.3.21 with AGP 9.2 built-in Kotlin support
- Gradle 9.4.1
- Jetpack Compose BOM 2026.04.01
- AndroidX Lifecycle 2.10.0
- kotlinx.coroutines 1.11.0
- compileSdk/targetSdk 36; minSdk 26

The project avoids networking, authentication, persistence, dependency-injection frameworks, and real CI integrations. The learning behavior remains visible and deterministic.

## Verification

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

GitHub Actions runs the same checks on every push and pull request.

## Official references

- [Kotlin coroutines overview](https://kotlinlang.org/docs/coroutines-overview.html)
- [Kotlin Flow guide](https://kotlinlang.org/docs/flow.html)
- [kotlinx.coroutines API](https://kotlinlang.org/api/kotlinx.coroutines/)
- [Android StateFlow and SharedFlow guidance](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Android Gradle Plugin 9.2 release notes](https://developer.android.com/build/releases/agp-9-2-0-release-notes)

## Contributing

Small corrections and clearer learning examples are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a change.

## License

Apache License 2.0. See [LICENSE](LICENSE).
