# BuildPulse Article 02: Cold Flow and Collection

## Purpose

Create the second cumulative BuildPulse checkpoint. Replace repeated one-shot history reads with a cold `Flow` whose work begins only when collected. Make independent execution visible through two collectors without introducing hot streams or channel builders early.

## Reader promise

After the article and optional Android lab, a reader can explain:

- why defining a cold Flow does not run its producer;
- why `collect()` is the start command;
- why two collectors normally execute the upstream block twice;
- why values are emitted sequentially by default;
- why cancelling the collecting coroutine stops that execution;
- when a cold Flow is the appropriate default.

## Cornerstone analogy

BuildPulse stores a CI replay script. Saving the script is Flow creation: nothing runs. Pressing **Run A** executes the script for Collector A. Pressing **Run B** executes the same script again from `QUEUED` for Collector B. Each run has its own timeline.

The analogy maps exactly:

| BuildPulse replay | Cold Flow |
|---|---|
| Saved replay script | `flow { ... }` definition |
| Press Run | `collect()` |
| One replay execution | One collection |
| Replay checkpoints | Emitted values |
| Stop a replay | Cancel the collecting coroutine |

## Scope

### Production behavior

- Add `BuildHistoryReader.observeHistory(buildId): Flow<BuildSnapshot>`.
- Implement `ColdBuildHistoryRepository` with `flow {}`.
- Emit `QUEUED`, `COMPILING`, `RUNNING_TESTS`, `SECURITY_SCAN`, `DEPLOYING`, and `SUCCEEDED` in order.
- Delay between values through an injected suspending pause so unit tests remain deterministic.
- Count collection attempts in the learning UI; count actual upstream executions through an injected callback in behavior tests.
- Add two independently startable and cancellable collector timelines to the existing screen.
- Preserve the Article 01 stale-snapshot experiment.

### Learning content

- Article title: **Cold Flow — Why Nothing Happens Until `collect()`**.
- Begin with the engineering limitation, then the replay-script analogy, then the Kotlin model.
- Include one small executable snippet and its expected output.
- Include a two-collector animation and a definition-versus-execution static diagram.
- Link back to Article 01 and forward to Article 03 when available.
- Link the exact Git tag, release, source files, and comparison.

### Explicitly deferred

- `callbackFlow`
- `channelFlow`
- `StateFlow`
- `SharedFlow`
- `stateIn`
- `shareIn`
- `Channel`
- buffering, conflation, retry, backpressure, and dispatcher changes

## Acceptance criteria

1. Creating the history Flow causes zero executions and zero emitted work.
2. First collection starts exactly one execution and receives all stages in order.
3. Second collection starts a new execution and receives its own full sequence from `QUEUED`.
4. Cancelling a collector before completion prevents later stages from reaching that collector.
5. Collector A and Collector B are independently visible and controllable in Compose.
6. Existing Article 01 experiment remains usable.
7. Article, diagrams, README, code, and tests agree on terminology and behavior.
8. `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` succeed.
9. One milestone commit is tagged `article-02-cold-flow` and published as a GitHub release.
10. Medium Article 02 is published, Article 01 links forward, and Article 02 links back.
11. LinkedIn post contains the animation and Medium link, scheduled for 2 September 2026 at 9:00 AM IST.

## Sources of truth

- Kotlin `flow` API: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow.html
- Kotlin `Flow` API: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/
- Kotlin Flow guide: https://kotlinlang.org/docs/coroutines-flow.html
