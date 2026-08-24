package io.github.buildpulse.dashboard

import io.github.buildpulse.model.BuildStage

data class BuildPulseUiState(
    val serverStage: BuildStage = BuildStage.QUEUED,
    val serverSequence: Int = 0,
    val fetchedStage: BuildStage? = null,
    val fetchedSequence: Int? = null,
    val isWorking: Boolean = false,
) {
    val isSnapshotStale: Boolean
        get() = fetchedStage != null && fetchedStage != serverStage
}
