# Article 02 Cold Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish Article 02 while shipping a runnable Android lab that proves cold Flow laziness, independent collection, ordering, and cancellation.

**Architecture:** A `BuildHistoryReader` contract returns a cold `Flow<BuildSnapshot>`. `ColdBuildHistoryRepository` owns deterministic sequential production. `BuildPulseViewModel` owns two collection jobs and exposes immutable timeline state through the existing Compose `State`; this intentionally avoids introducing `StateFlow` before Article 06.

**Tech Stack:** Kotlin 2.3.21, kotlinx.coroutines 1.11.0, AndroidX ViewModel, Jetpack Compose, JUnit 4, Markdown, SVG/GIF.

**Spec:** `docs/superpowers/specs/2026-08-31-article-02-cold-flow.md`

## Global Constraints

- Preserve the Article 01 stale-snapshot experiment.
- Do not use `callbackFlow`, `channelFlow`, `StateFlow`, `SharedFlow`, `stateIn`, `shareIn`, or `Channel`.
- Article remains understandable without the repository.
- One published article equals one instructional commit, tag, and GitHub release.
- Use Brave for Medium and LinkedIn publishing.

---

### Task 1: Cold history contract and repository

**Files:**
- Modify: `app/src/main/java/io/github/buildpulse/simulation/BuildContracts.kt`
- Create: `app/src/main/java/io/github/buildpulse/simulation/ColdBuildHistoryRepository.kt`
- Create: `app/src/test/java/io/github/buildpulse/simulation/ColdBuildHistoryRepositoryTest.kt`

**Interfaces:**
- Produces: `fun interface BuildHistoryReader { fun observeHistory(buildId: BuildId): Flow<BuildSnapshot> }`
- Produces: `class ColdBuildHistoryRepository(...): BuildHistoryReader`

- [ ] **Step 1: Write failing tests**

```kotlin
@Test fun `defining history does not start an execution`() = runTest {
    var starts = 0
    val repository = repository(onExecutionStarted = { starts++ })
    repository.observeHistory(BuildId("build-42"))
    assertEquals(0, starts)
}

@Test fun `each collector starts an independent ordered execution`() = runTest {
    var starts = 0
    val repository = repository(onExecutionStarted = { starts++ })
    val history = repository.observeHistory(BuildId("build-42"))
    assertEquals(expectedStages, history.map { it.stage }.toList())
    assertEquals(expectedStages, history.map { it.stage }.toList())
    assertEquals(2, starts)
}

@Test fun `cancelling collection prevents later emissions`() = runTest {
    val repository = repository()
    val received = repository.observeHistory(BuildId("build-42")).take(2).toList()
    assertEquals(listOf(BuildStage.QUEUED, BuildStage.COMPILING), received.map { it.stage })
}
```

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*ColdBuildHistoryRepositoryTest'`

Expected: compilation failure because `ColdBuildHistoryRepository` and `BuildHistoryReader` do not exist.

- [ ] **Step 3: Implement minimal production behavior**

```kotlin
fun interface BuildHistoryReader {
    fun observeHistory(buildId: BuildId): Flow<BuildSnapshot>
}

class ColdBuildHistoryRepository(
    private val pauseBetweenStages: suspend () -> Unit = { delay(700) },
    private val onExecutionStarted: () -> Unit = {},
) : BuildHistoryReader {
    override fun observeHistory(buildId: BuildId): Flow<BuildSnapshot> = flow {
        onExecutionStarted()
        BuildStage.entries.forEachIndexed { index, stage ->
            emit(BuildSnapshot(buildId, stage, index))
            if (stage != BuildStage.SUCCEEDED) pauseBetweenStages()
        }
    }
}
```

- [ ] **Step 4: Run GREEN**

Run: `./gradlew testDebugUnitTest --tests '*ColdBuildHistoryRepositoryTest'`

Expected: all repository tests pass.

### Task 2: Two independent collector timelines

**Files:**
- Modify: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseUiState.kt`
- Modify: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseViewModel.kt`
- Modify: `app/src/test/java/io/github/buildpulse/dashboard/BuildPulseViewModelTest.kt`

**Interfaces:**
- Consumes: `BuildHistoryReader.observeHistory(buildId)`
- Produces: `CollectorTimeline`, `startCollector(id)`, and `stopCollector(id)`

- [ ] **Step 1: Write failing ViewModel tests**

```kotlin
@Test fun `starting collector A records cold flow emissions`() = runTest {
    val viewModel = viewModelWithImmediateHistory()
    viewModel.startCollector(CollectorId.A)
    advanceUntilIdle()
    assertEquals(expectedStages, viewModel.uiState.value.collectorA.snapshots.map { it.stage })
}

@Test fun `collector B starts its own history from queued`() = runTest {
    val viewModel = viewModelWithControllableHistory()
    viewModel.startCollector(CollectorId.A)
    runCurrent()
    viewModel.startCollector(CollectorId.B)
    runCurrent()
    assertEquals(BuildStage.QUEUED, viewModel.uiState.value.collectorA.snapshots.first().stage)
    assertEquals(BuildStage.QUEUED, viewModel.uiState.value.collectorB.snapshots.first().stage)
}
```

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*BuildPulseViewModelTest'`

Expected: compilation failure because collector APIs do not exist.

- [ ] **Step 3: Implement collector state and jobs**

Use one `Job?` per collector. Starting cancels only that collector's prior job, clears its timeline, marks it running, and collects the same cold Flow. Each emission appends one snapshot. Completion and cancellation mark only that collector not running.

- [ ] **Step 4: Run GREEN**

Run: `./gradlew testDebugUnitTest --tests '*BuildPulseViewModelTest'`

Expected: all ViewModel tests pass.

### Task 3: Compose learning lab

**Files:**
- Modify: `app/src/main/java/io/github/buildpulse/MainActivity.kt`
- Modify: `app/src/main/java/io/github/buildpulse/dashboard/BuildPulseScreen.kt`

**Interfaces:**
- Consumes: collector state and `startCollector`/`stopCollector` callbacks.
- Produces: two visible timelines with Run/Stop controls and exact cold-flow memory rules.

- [ ] **Step 1: Inject `ColdBuildHistoryRepository` into `BuildPulseViewModel`.**
- [ ] **Step 2: Retain Article 01 cards and add Article 02 replay lab below them.**
- [ ] **Step 3: Render Collector A and B as separate ordered stage lists.**
- [ ] **Step 4: Add copy: “Defining waits. Collecting runs. Every collector gets a new execution.”**
- [ ] **Step 5: Compile UI**

Run: `./gradlew :app:compileDebugKotlin`

Expected: build succeeds.

### Task 4: Learning article and visuals

**Files:**
- Create: `articles/02-cold-flow.md`
- Create: `diagrams/article-02-definition-vs-collection.svg`
- Create: `diagrams/article-02-definition-vs-collection.png`
- Create: `diagrams/article-02-two-collectors.gif`
- Modify: `README.md`

**Interfaces:**
- Article links Article 01, source/tests, exact tag/release, and official Kotlin sources.
- Visuals show no producer activity before `collect`, then two independent executions.

- [ ] **Step 1: Draft the article from problem to analogy to exact semantics.**
- [ ] **Step 2: Include the minimal snippet and deterministic output.**
- [ ] **Step 3: Generate visuals with large readable English labels and alt text.**
- [ ] **Step 4: Render and visually inspect animation frames and static image.**
- [ ] **Step 5: Update README milestone and roadmap.**

### Task 5: Verify and publish milestone

**Files:** all changed milestone files.

- [ ] **Step 1: Run complete checks**

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
git diff --check
```

- [ ] **Step 2: Review graph impact and changed-file context.**
- [ ] **Step 3: Create one instructional commit:** `article-02: teach cold Flow collection`.
- [ ] **Step 4: Merge the verified branch into `main`, push, tag `article-02-cold-flow`, and publish the GitHub release.**
- [ ] **Step 5: Publish Medium Article 02 in Brave and obtain its public URL.**
- [ ] **Step 6: Edit Article 01 so its Next link points to Article 02; verify Article 02 points back.**
- [ ] **Step 7: Schedule LinkedIn for Wednesday 2 September 2026, 9:00 AM IST with the GIF and Medium link; remove the generated link-preview card before attaching media.**
- [ ] **Step 8: Verify public GitHub, Medium cross-links, and LinkedIn scheduled-entry details.**
