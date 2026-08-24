package io.github.buildpulse.dashboard

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.simulation.InMemoryBuildSimulator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
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
}
