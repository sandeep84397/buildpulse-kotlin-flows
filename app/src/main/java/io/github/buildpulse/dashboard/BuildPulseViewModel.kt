package io.github.buildpulse.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.buildpulse.model.BuildId
import io.github.buildpulse.simulation.BuildScenarioController
import io.github.buildpulse.simulation.BuildStatusReader
import kotlinx.coroutines.launch

class BuildPulseViewModel(
    private val statusReader: BuildStatusReader,
    private val scenarioController: BuildScenarioController,
    private val buildId: BuildId,
) : ViewModel() {
    private val _uiState = mutableStateOf(BuildPulseUiState())
    val uiState: State<BuildPulseUiState> = _uiState

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
}
