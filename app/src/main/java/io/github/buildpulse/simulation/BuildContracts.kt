package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import kotlinx.coroutines.flow.Flow

fun interface BuildStatusReader {
    suspend fun fetchStatus(buildId: BuildId): BuildSnapshot
}

fun interface BuildHistoryReader {
    fun observeHistory(buildId: BuildId): Flow<BuildSnapshot>
}

interface BuildScenarioController {
    suspend fun currentServerStatus(buildId: BuildId): BuildSnapshot

    suspend fun advanceServer(buildId: BuildId): BuildSnapshot
}
