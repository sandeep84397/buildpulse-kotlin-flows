package io.github.buildpulse.dashboard

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.simulation.ColdBuildHistoryRepository
import io.github.buildpulse.simulation.InMemoryBuildSimulator
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuildPulseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `server advance leaves fetched UI snapshot stale until next fetch`() = runTest {
        val simulator = InMemoryBuildSimulator()
        val viewModel = BuildPulseViewModel(
            statusReader = simulator,
            scenarioController = simulator,
            historyReader = ColdBuildHistoryRepository(pauseBetweenStages = {}),
            buildId = BuildId("build-42"),
        )

        viewModel.fetchSnapshot()
        advanceUntilIdle()

        assertEquals(BuildStage.QUEUED, viewModel.uiState.value.fetchedStage)
        assertFalse(viewModel.uiState.value.isSnapshotStale)

        viewModel.advanceServer()
        advanceUntilIdle()

        assertEquals(BuildStage.COMPILING, viewModel.uiState.value.serverStage)
        assertEquals(BuildStage.QUEUED, viewModel.uiState.value.fetchedStage)
        assertTrue(viewModel.uiState.value.isSnapshotStale)
    }

    @Test
    fun `starting collector A records every cold history emission`() = runTest {
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(pauseBetweenStages = {}),
        )

        viewModel.startCollector(CollectorId.A)
        advanceUntilIdle()

        assertEquals(
            BuildStage.entries,
            viewModel.uiState.value.collectorA.snapshots.map { it.stage },
        )
        assertFalse(viewModel.uiState.value.collectorA.isRunning)
        assertTrue(viewModel.uiState.value.collectorA.isComplete)
    }

    @Test
    fun `collector B starts a separate execution from queued`() = runTest {
        var executionStarts = 0
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(
                pauseBetweenStages = {},
                onExecutionStarted = { executionStarts++ },
            ),
        )

        viewModel.startCollector(CollectorId.A)
        advanceUntilIdle()
        viewModel.startCollector(CollectorId.B)
        advanceUntilIdle()

        assertEquals(BuildStage.QUEUED, viewModel.uiState.value.collectorA.snapshots.first().stage)
        assertEquals(BuildStage.QUEUED, viewModel.uiState.value.collectorB.snapshots.first().stage)
        assertEquals(BuildStage.entries, viewModel.uiState.value.collectorA.snapshots.map { it.stage })
        assertEquals(BuildStage.entries, viewModel.uiState.value.collectorB.snapshots.map { it.stage })
        assertEquals(2, executionStarts)
    }

    @Test
    fun `stopping collector A does not stop collector B`() = runTest {
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(
                pauseBetweenStages = { awaitCancellation() },
            ),
        )

        viewModel.startCollector(CollectorId.A)
        viewModel.startCollector(CollectorId.B)
        runCurrent()

        assertEquals(listOf(BuildStage.QUEUED), viewModel.uiState.value.collectorA.snapshots.map { it.stage })
        assertEquals(listOf(BuildStage.QUEUED), viewModel.uiState.value.collectorB.snapshots.map { it.stage })
        assertTrue(viewModel.uiState.value.collectorA.isRunning)
        assertTrue(viewModel.uiState.value.collectorB.isRunning)

        viewModel.stopCollector(CollectorId.A)
        runCurrent()

        assertFalse(viewModel.uiState.value.collectorA.isRunning)
        assertTrue(viewModel.uiState.value.collectorB.isRunning)
        viewModel.stopCollector(CollectorId.B)
        runCurrent()
    }

    private fun viewModel(
        historyRepository: ColdBuildHistoryRepository,
    ): BuildPulseViewModel {
        val simulator = InMemoryBuildSimulator()
        return BuildPulseViewModel(
            statusReader = simulator,
            scenarioController = simulator,
            historyReader = historyRepository,
            buildId = BuildId("build-42"),
        )
    }
}
