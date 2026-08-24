package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot

fun interface BuildStatusReader {
    suspend fun fetchStatus(buildId: BuildId): BuildSnapshot
}

interface BuildScenarioController {
    suspend fun currentServerStatus(buildId: BuildId): BuildSnapshot

    suspend fun advanceServer(buildId: BuildId): BuildSnapshot
}
