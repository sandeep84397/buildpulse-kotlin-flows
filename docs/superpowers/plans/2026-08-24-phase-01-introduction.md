# BuildPulse Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and publish the first runnable BuildPulse checkpoint that demonstrates why one suspending result cannot represent a changing CI/CD build.

**Architecture:** A deterministic in-memory simulator implements separate one-shot read and scenario-control interfaces. A ViewModel stores an immutable fetched snapshot separately from the current simulated server status, and a Compose screen makes the stale-data gap visible. No Kotlin Flow API is used in production code during this milestone.

**Tech Stack:** Kotlin 2.3.21, Android Gradle Plugin 9.2.0, Gradle 9.4.1, Jetpack Compose BOM 2026.04.01, AndroidX Activity 1.13.0, Lifecycle 2.10.0, kotlinx.coroutines 1.11.0, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-08-24-phase-01-introduction.md`

## Global Constraints

- Public repository name is `buildpulse-kotlin-flows`.
- Phase 1 production code must not use Flow, StateFlow, SharedFlow, Channel, callbackFlow, or channelFlow.
- App must run entirely offline after dependencies are resolved.
- Main history receives exactly one instructional commit for Article 1.
- Tag is `article-01-introduction`.
- Medium remains a draft until explicit publishing approval.
- Browser-based publication uses Brave only.
- The article is self-contained and concept-first; GitHub is an optional supporting lab.
- Article code is limited to five focused snippets, normally no more than twelve lines each.

---

## File map

- `settings.gradle.kts`: repositories and module registration.
- `build.gradle.kts`: root plugin versions.
- `gradle/libs.versions.toml`: dependency catalog.
- `app/build.gradle.kts`: Android application configuration.
- `app/src/main/.../model/BuildModels.kt`: immutable build identifiers, stages, and snapshots.
- `app/src/main/.../simulation/BuildContracts.kt`: one-shot read and simulator-control interfaces.
- `app/src/main/.../simulation/InMemoryBuildSimulator.kt`: deterministic progression.
- `app/src/main/.../dashboard/BuildPulseUiState.kt`: screen state.
- `app/src/main/.../dashboard/BuildPulseViewModel.kt`: asynchronous action coordination.
- `app/src/main/.../dashboard/BuildPulseScreen.kt`: Compose experiment UI.
- `app/src/main/.../MainActivity.kt`: app entry point and dependency assembly.
- `app/src/test/...`: domain, simulator, and ViewModel behavior tests.
- `articles/01-asynchronous-streams.md`: complete Medium-ready article.
- `README.md`: repository entry point and learning roadmap.
- `.github/workflows/android.yml`: reproducible test/build gate.

### Task 1: Project foundation and first failing domain test

**Files:**
- Create: Gradle wrapper and root build files
- Create: `app/src/test/java/io/github/buildpulse/model/BuildStageTest.kt`
- Create after RED: `app/src/main/java/io/github/buildpulse/model/BuildModels.kt`

**Interfaces:**
- Produces: `enum class BuildStage`, `fun BuildStage.next(): BuildStage`, `@JvmInline value class BuildId(val value: String)`, `data class BuildSnapshot(...)`.

- [ ] Write a test asserting `QUEUED.next()` progresses through all six stages and `SUCCEEDED.next()` stays `SUCCEEDED`.
- [ ] Run `./gradlew testDebugUnitTest --tests '*BuildStageTest'`; verify compilation fails because `BuildStage` does not exist.
- [ ] Implement the minimal immutable model and explicit `when` progression.
- [ ] Re-run the focused test and verify it passes.

### Task 2: Simulator stale-snapshot behavior

**Files:**
- Create: `app/src/test/java/io/github/buildpulse/simulation/InMemoryBuildSimulatorTest.kt`
- Create after RED: `app/src/main/java/io/github/buildpulse/simulation/BuildContracts.kt`
- Create after RED: `app/src/main/java/io/github/buildpulse/simulation/InMemoryBuildSimulator.kt`

**Interfaces:**
- Consumes: `BuildId`, `BuildSnapshot`, `BuildStage.next()`.
- Produces: `BuildStatusReader.fetchStatus(BuildId)`, `BuildScenarioController.currentServerStatus(BuildId)`, `BuildScenarioController.advanceServer(BuildId)`.

- [ ] Write a coroutine test that fetches `QUEUED`, advances the server, and asserts the earlier snapshot remains `QUEUED` while the server becomes `COMPILING`.
- [ ] Run the focused test; verify it fails because simulator contracts are missing.
- [ ] Implement a `Mutex`-protected in-memory map initialized at `QUEUED`; return new immutable snapshots on every call.
- [ ] Re-run simulator and domain tests; verify both pass.

### Task 3: ViewModel exposes the learning experiment

**Files:**
- Create: `app/src/test/java/io/github/buildpulse/dashboard/MainDispatcherRule.kt`
- Create: `app/src/test/java/io/github/buildpulse/dashboard/BuildPulseViewModelTest.kt`
- Create after RED: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseUiState.kt`
- Create after RED: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseViewModel.kt`

**Interfaces:**
- Consumes: `BuildStatusReader`, `BuildScenarioController`, `BuildId`.
- Produces: `val uiState: State<BuildPulseUiState>`, `fetchSnapshot()`, `advanceServer()`.

- [ ] Write a test that fetches a snapshot, advances the server, and proves `serverStage == COMPILING` while `fetchedStage == QUEUED` and `isSnapshotStale == true`.
- [ ] Run the focused test; verify it fails because the ViewModel does not exist.
- [ ] Implement the ViewModel with Compose `mutableStateOf`; launch suspend calls in `viewModelScope`; never auto-refresh `fetchedStage` during `advanceServer()`.
- [ ] Re-run all unit tests; verify they pass.

### Task 4: Compose dashboard and Android entry point

**Files:**
- Create: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseScreen.kt`
- Create: `app/src/main/java/io/github/buildpulse/MainActivity.kt`
- Create: `app/src/main/AndroidManifest.xml`
- Create: minimal theme resources.

**Interfaces:**
- Consumes: `BuildPulseUiState`, `fetchSnapshot()`, `advanceServer()`.
- Produces: runnable single-activity Compose application.

- [ ] Render server stage and fetched snapshot as distinct cards.
- [ ] Add `Advance server` and `Fetch snapshot` actions with stable semantic labels.
- [ ] Show `Snapshot is stale` only when the two stages differ.
- [ ] Add the nine-step series roadmap with Article 1 highlighted.
- [ ] Run `./gradlew :app:compileDebugKotlin`; verify exit code 0.

### Task 5: Cornerstone article and repository guide

**Files:**
- Create: `articles/01-asynchronous-streams.md`
- Create: `README.md`
- Create: `LICENSE`
- Create: `CONTRIBUTING.md`
- Create: `.github/workflows/android.yml`

**Interfaces:**
- Produces: Medium-ready article, runnable instructions, series navigation, CI gate.

- [ ] Write the article using the BuildPulse scenario and problem-first structure from the spec; keep approximately 80% of the narrative focused on concepts, reasoning, timelines, and mental models.
- [ ] Make the article understandable without cloning the repository; defer all setup and run instructions to the optional lab near the end.
- [ ] Use at most five short Kotlin snippets, each answering one explicit learning question; do not paste complete project files.
- [ ] Explain cold stream, hot stream, and Channel only at high level; link future articles using explicit unpublished markers.
- [ ] Add misconception checks, terminology explanations, a recall exercise, and the limits of the BuildPulse analogy.
- [ ] Add links to the Article 1 tag/release and exact source packages.
- [ ] Write README setup, screenshot placeholder, roadmap, milestone table, and official references.
- [ ] Configure GitHub Actions to run `testDebugUnitTest` and `assembleDebug` with JDK 17.
- [ ] Scan Markdown for broken relative repository links.

### Task 6: Verification and public milestone

**Files:**
- Inspect all Phase 1 files; no new production behavior.

- [ ] Run `./gradlew testDebugUnitTest` and record passed test count.
- [ ] Run `./gradlew assembleDebug` and record exit code.
- [ ] Scan production Kotlin for forbidden future APIs.
- [ ] Run `git diff --check` and inspect `git status --short`.
- [ ] Initialize Git if needed, create branch `article/01-introduction`, and stage only confirmed project paths.
- [ ] Create exactly one commit: `article-01: establish the asynchronous-stream problem`.
- [ ] Rename the branch to `main` and create annotated tag `article-01-introduction`.
- [ ] Create public GitHub repository `buildpulse-kotlin-flows`, push `main` and the tag, then verify visibility and commit count.
- [ ] Create GitHub Release `Article 01 — Asynchronous Streams Introduction` with run instructions and an explicit `Medium article: draft` marker.

### Task 7: Medium draft handoff

**Files:**
- Reuse: `articles/01-asynchronous-streams.md`

- [ ] Open Medium in Brave only when ready for browser work.
- [ ] Create or update a draft; do not publish.
- [ ] Add the public GitHub tag and release links.
- [ ] Present the article, repository URL, release URL, and verification evidence for user review.
- [ ] Publish only after explicit user approval.

## Self-review record

- Spec coverage: every functional, content, version, history, and publication constraint maps to Tasks 1–7.
- Placeholder policy: future article URLs are intentionally labeled `unpublished`, not silent implementation placeholders.
- Type consistency: simulator and ViewModel signatures are defined once and consumed consistently.
- Scope control: no networking, persistence, authentication, real CI integration, or Flow implementation.
