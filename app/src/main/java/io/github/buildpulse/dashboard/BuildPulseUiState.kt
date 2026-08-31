package io.github.buildpulse.dashboard

import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.BuildSnapshot

enum class CollectorId {
    A,
    B,
}

data class CollectorTimeline(
    val collectionNumber: Int = 0,
    val snapshots: List<BuildSnapshot> = emptyList(),
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
)

data class BuildPulseUiState(
    val serverStage: BuildStage = BuildStage.QUEUED,
    val serverSequence: Int = 0,
    val fetchedStage: BuildStage? = null,
    val fetchedSequence: Int? = null,
    val isWorking: Boolean = false,
    val collectorA: CollectorTimeline = CollectorTimeline(),
    val collectorB: CollectorTimeline = CollectorTimeline(),
) {
    val isSnapshotStale: Boolean
        get() = fetchedStage != null && fetchedStage != serverStage
}
