package io.github.buildpulse.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.buildpulse.model.BuildId
import io.github.buildpulse.simulation.BuildHistoryReader
import io.github.buildpulse.simulation.BuildReportReader
import io.github.buildpulse.simulation.BuildScenarioController
import io.github.buildpulse.simulation.BuildStatusReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BuildPulseViewModel(
    private val statusReader: BuildStatusReader,
    private val scenarioController: BuildScenarioController,
    private val historyReader: BuildHistoryReader,
    private val reportReader: BuildReportReader,
    private val buildId: BuildId,
) : ViewModel() {
    private val _uiState = mutableStateOf(BuildPulseUiState())
    val uiState: State<BuildPulseUiState> = _uiState
    private val collectorJobs = mutableMapOf<CollectorId, Job>()
    private var concurrentRunJob: Job? = null

    fun fetchSnapshot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true)
            val snapshot = statusReader.fetchStatus(buildId)
            _uiState.value = _uiState.value.copy(
                serverStage = snapshot.stage,
                serverSequence = snapshot.sequence,
                fetchedStage = snapshot.stage,
                fetchedSequence = snapshot.sequence,
                isWorking = false,
            )
        }
    }

    fun advanceServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true)
            val server = scenarioController.advanceServer(buildId)
            _uiState.value = _uiState.value.copy(
                serverStage = server.stage,
                serverSequence = server.sequence,
                isWorking = false,
            )
        }
    }

    fun startCollector(collectorId: CollectorId) {
        collectorJobs.remove(collectorId)?.cancel()
        val collectionNumber = timeline(collectorId).collectionNumber + 1
        updateTimeline(collectorId) {
            CollectorTimeline(
                collectionNumber = collectionNumber,
                isRunning = true,
            )
        }

        collectorJobs[collectorId] = viewModelScope.launch {
            var completed = false
            try {
                historyReader.observeHistory(buildId).collect { snapshot ->
                    updateTimelineIfCurrent(collectorId, collectionNumber) { timeline ->
                        timeline.copy(snapshots = timeline.snapshots + snapshot)
                    }
                }
                completed = true
            } finally {
                updateTimelineIfCurrent(collectorId, collectionNumber) { timeline ->
                    timeline.copy(
                        isRunning = false,
                        isComplete = completed,
                    )
                }
            }
        }
    }

    fun stopCollector(collectorId: CollectorId) {
        collectorJobs.remove(collectorId)?.cancel()
        updateTimeline(collectorId) { it.copy(isRunning = false) }
    }

    fun startConcurrentRun() {
        concurrentRunJob?.cancel()
        val runNumber = _uiState.value.concurrentRun.runNumber + 1
        _uiState.value = _uiState.value.copy(
            concurrentRun = ConcurrentRunTimeline(
                runNumber = runNumber,
                isRunning = true,
            ),
        )

        concurrentRunJob = viewModelScope.launch {
            var completed = false
            try {
                reportReader.observeReports(buildId).collect { report ->
                    updateConcurrentRunIfCurrent(runNumber) { timeline ->
                        timeline.copy(reports = timeline.reports + report)
                    }
                }
                completed = true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                updateConcurrentRunIfCurrent(runNumber) { timeline ->
                    timeline.copy(errorMessage = failure.message ?: failure::class.simpleName)
                }
            } finally {
                updateConcurrentRunIfCurrent(runNumber) { timeline ->
                    timeline.copy(
                        isRunning = false,
                        isComplete = completed,
                    )
                }
            }
        }
    }

    fun stopConcurrentRun() {
        concurrentRunJob?.cancel()
        concurrentRunJob = null
        _uiState.value = _uiState.value.copy(
            concurrentRun = _uiState.value.concurrentRun.copy(isRunning = false),
        )
    }

    private fun timeline(collectorId: CollectorId): CollectorTimeline = when (collectorId) {
        CollectorId.A -> _uiState.value.collectorA
        CollectorId.B -> _uiState.value.collectorB
    }

    private fun updateTimelineIfCurrent(
        collectorId: CollectorId,
        collectionNumber: Int,
        transform: (CollectorTimeline) -> CollectorTimeline,
    ) {
        if (timeline(collectorId).collectionNumber == collectionNumber) {
            updateTimeline(collectorId, transform)
        }
    }

    private fun updateTimeline(
        collectorId: CollectorId,
        transform: (CollectorTimeline) -> CollectorTimeline,
    ) {
        _uiState.value = when (collectorId) {
            CollectorId.A -> _uiState.value.copy(collectorA = transform(_uiState.value.collectorA))
            CollectorId.B -> _uiState.value.copy(collectorB = transform(_uiState.value.collectorB))
        }
    }

    private fun updateConcurrentRunIfCurrent(
        runNumber: Int,
        transform: (ConcurrentRunTimeline) -> ConcurrentRunTimeline,
    ) {
        if (_uiState.value.concurrentRun.runNumber == runNumber) {
            _uiState.value = _uiState.value.copy(
                concurrentRun = transform(_uiState.value.concurrentRun),
            )
        }
    }
}
