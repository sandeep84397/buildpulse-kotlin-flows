# BuildPulse Phase 1: Asynchronous Streams Introduction

## Purpose

Create the first cumulative, runnable checkpoint for the BuildPulse article series. The project demonstrates why a one-shot asynchronous result becomes stale when the underlying build continues changing. It introduces the problem that later articles solve; it does not implement a Flow solution yet.

## Reader promise

After reading Article 1 and running the app, a reader can explain:

- synchronous result versus suspending result;
- why `suspend` solves waiting but still returns one value;
- why continuously changing data requires a stream;
- the high-level roles of cold streams, hot streams, and channels;
- where the series begins and what each later milestone will add.

## Pedagogy contract

The Medium article is the primary learning product. The GitHub repository is supporting evidence and an optional lab; it must never become the narrative.

- The article must remain understandable without cloning, building, or reading the repository.
- Begin with the engineering problem and observable consequences, not an API definition.
- Introduce one question at a time: what changed, why the previous tool is insufficient, and which new mental model is needed.
- Prefer timelines, comparisons, questions, and plain-language reasoning over setup instructions.
- Use no more than five Kotlin snippets in the article; each snippet should normally fit within twelve lines and answer one specific question.
- Never paste complete project files into Medium.
- Place repository links after the explanation they support, and collect the runnable lab near the end.
- Explain terminology by name: why `suspend`, `stream`, `cold`, `hot`, and `channel` are called that.
- State the limits of every analogy before presenting exact Kotlin semantics.
- End with a recall exercise and decision questions, not merely a finished code sample.

The target balance is roughly 80% concepts, reasoning, timelines, and mental models; 20% focused code and optional experimentation.

## Cornerstone scenario

BuildPulse monitors a CI/CD build moving through:

`QUEUED -> COMPILING -> RUNNING_TESTS -> SECURITY_SCAN -> DEPLOYING -> SUCCEEDED`

The server progresses independently. The app calls `fetchStatus()` and receives one immutable snapshot. When the simulated server advances, the displayed snapshot remains unchanged until the user fetches again. This visible mismatch is the Phase 1 teaching mechanism.

## Functional scope

The Android app must:

- run without accounts, API keys, network access, or a backend;
- show the simulated server status and the last fetched UI snapshot side by side;
- provide `Advance server` and `Fetch snapshot` actions;
- make the stale snapshot explicit with plain-language copy;
- show the nine-article learning roadmap;
- use a single activity and Jetpack Compose;
- remain intentionally free of Kotlin Flow APIs in production code.

## Architecture

- `BuildStatusReader`: one-shot asynchronous read contract.
- `BuildScenarioController`: learning-only simulator control contract.
- `InMemoryBuildSimulator`: deterministic implementation of both contracts.
- `BuildPulseViewModel`: coordinates actions and exposes Compose snapshot state.
- `BuildPulseScreen`: renders the experiment and roadmap.

The two interfaces separate the realistic read API from the artificial teaching control. Later articles may replace `BuildStatusReader` with streaming repository contracts without coupling the UI to simulator internals.

## Content scope

Article title: **Building a Live CI/CD Monitor: Why Asynchronous Streams Exist in Kotlin**

Article structure:

1. A build keeps changing after the first response.
2. Immediate return value.
3. One suspending result.
4. Why repeated polling is incomplete.
5. Definition of asynchronous stream.
6. High-level cold stream, hot stream, and channel map.
7. BuildPulse mapping table.
8. Misconceptions and recall checks.
9. Optional Phase 1 experiment and result.
10. Series roadmap and direct links/placeholders.
11. Next article: Cold Flow.

The article uses clean English, one central scenario, short Kotlin examples, and an explicit boundary between analogy and exact coroutine semantics.

## Repository and publication contract

- Repository name: `buildpulse-kotlin-flows`.
- Visibility: public.
- License: Apache-2.0.
- Default branch: `main`.
- Exactly one instructional milestone commit for Article 1.
- Commit: `article-01: establish the asynchronous-stream problem`.
- Tag: `article-01-introduction`.
- GitHub Release: `Article 01 — Asynchronous Streams Introduction`.
- Medium remains a draft until explicit user approval to publish.
- Medium/LinkedIn browser operations use Brave, never Chrome.

## Toolchain

- JDK 17 bytecode target.
- Android Gradle Plugin 9.2.0.
- Gradle 9.4.1.
- Kotlin 2.3.21.
- Compose BOM 2026.04.01 (Compose 1.11 stable; compatible with API 36).
- AndroidX Activity Compose 1.13.0.
- AndroidX Lifecycle 2.10.0 (stable API 36-compatible line).
- kotlinx.coroutines 1.11.0.
- compileSdk/targetSdk 36; minSdk 26.

Versions follow current official Android and Kotlin documentation as of 2026-08-24.

## Verification

- Domain tests prove stage progression and terminal behavior.
- Simulator tests prove previously fetched snapshots stay immutable after server advancement.
- ViewModel tests prove server advancement does not silently refresh the UI snapshot.
- Debug APK compiles.
- Unit-test suite passes.
- `git diff --check` passes.
- Production source scan contains no `Flow`, `StateFlow`, `SharedFlow`, `Channel`, `flow`, `callbackFlow`, or `channelFlow` usage.
- Git history contains one commit after publication and the milestone tag points to it.

## Out of scope

- Real CI provider integration.
- Authentication, storage, networking, dependency-injection frameworks.
- Flow-based production implementation.
- Medium publication without user approval.
- LinkedIn content or publication unless separately requested.
