package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.next
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryBuildSimulator : BuildStatusReader, BuildScenarioController {
    private val mutex = Mutex()
    private val snapshots = mutableMapOf<BuildId, BuildSnapshot>()

    override suspend fun fetchStatus(buildId: BuildId): BuildSnapshot =
        currentServerStatus(buildId)

    override suspend fun currentServerStatus(buildId: BuildId): BuildSnapshot = mutex.withLock {
        snapshots.getOrPut(buildId) { initialSnapshot(buildId) }
    }

    override suspend fun advanceServer(buildId: BuildId): BuildSnapshot = mutex.withLock {
        val current = snapshots.getOrPut(buildId) { initialSnapshot(buildId) }
        val nextStage = current.stage.next()
        val advanced = current.copy(
            stage = nextStage,
            sequence = if (nextStage == current.stage) current.sequence else current.sequence + 1,
        )
        snapshots[buildId] = advanced
        advanced
    }

    private fun initialSnapshot(buildId: BuildId) = BuildSnapshot(
        buildId = buildId,
        stage = BuildStage.QUEUED,
        sequence = 0,
    )
}
