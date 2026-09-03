package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.next
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArraySet

class InMemoryBuildSimulator : BuildStatusReader, BuildScenarioController, BuildCallbackSource {
    private val mutex = Mutex()
    private val snapshots = mutableMapOf<BuildId, BuildSnapshot>()
    private val listeners = CopyOnWriteArraySet<BuildUpdateListener>()

    override suspend fun fetchStatus(buildId: BuildId): BuildSnapshot =
        currentServerStatus(buildId)

    override suspend fun currentServerStatus(buildId: BuildId): BuildSnapshot = mutex.withLock {
        snapshots.getOrPut(buildId) { initialSnapshot(buildId) }
    }

    override suspend fun advanceServer(buildId: BuildId): BuildSnapshot {
        var didAdvance = false
        val advanced = mutex.withLock {
            val current = snapshots.getOrPut(buildId) { initialSnapshot(buildId) }
            val nextStage = current.stage.next()
            didAdvance = nextStage != current.stage
            current.copy(
                stage = nextStage,
                sequence = if (didAdvance) current.sequence + 1 else current.sequence,
            ).also { snapshots[buildId] = it }
        }

        if (didAdvance) {
            listeners.forEach { it.onBuildUpdated(advanced) }
        }
        return advanced
    }

    override fun addListener(listener: BuildUpdateListener) {
        listeners += listener
    }

    override fun removeListener(listener: BuildUpdateListener) {
        listeners -= listener
    }

    private fun initialSnapshot(buildId: BuildId) = BuildSnapshot(
        buildId = buildId,
        stage = BuildStage.QUEUED,
        sequence = 0,
    )
}
