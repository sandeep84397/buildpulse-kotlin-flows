package io.github.buildpulse.dashboard

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildReport
import io.github.buildpulse.model.BuildReportState
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.BuildTask
import io.github.buildpulse.simulation.BuildReportReader
import io.github.buildpulse.simulation.ColdBuildHistoryRepository
import io.github.buildpulse.simulation.InMemoryBuildSimulator
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
            reportReader = BuildReportReader { emptyFlow() },
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

    @Test
    fun `starting concurrent run records merged reports then marks execution complete`() = runTest {
        val buildId = BuildId("build-42")
        val reports = listOf(
            BuildReport(buildId, BuildTask.COMPILER, BuildReportState.STARTED),
            BuildReport(buildId, BuildTask.TESTS, BuildReportState.STARTED),
            BuildReport(buildId, BuildTask.COMPILER, BuildReportState.COMPLETED),
            BuildReport(buildId, BuildTask.SECURITY_SCAN, BuildReportState.COMPLETED),
        )
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(pauseBetweenStages = {}),
            reportReader = BuildReportReader { flowOf(*reports.toTypedArray()) },
        )

        viewModel.startConcurrentRun()
        advanceUntilIdle()

        assertEquals(reports, viewModel.uiState.value.concurrentRun.reports)
        assertEquals(1, viewModel.uiState.value.concurrentRun.runNumber)
        assertFalse(viewModel.uiState.value.concurrentRun.isRunning)
        assertTrue(viewModel.uiState.value.concurrentRun.isComplete)
    }

    @Test
    fun `stopping concurrent run cancels its collection`() = runTest {
        var cancellations = 0
        val buildId = BuildId("build-42")
        val reportReader = BuildReportReader {
            flow {
                emit(BuildReport(buildId, BuildTask.COMPILER, BuildReportState.STARTED))
                try {
                    awaitCancellation()
                } finally {
                    cancellations++
                }
            }
        }
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(pauseBetweenStages = {}),
            reportReader = reportReader,
        )

        viewModel.startConcurrentRun()
        runCurrent()
        viewModel.stopConcurrentRun()
        runCurrent()

        assertEquals(1, cancellations)
        assertFalse(viewModel.uiState.value.concurrentRun.isRunning)
        assertFalse(viewModel.uiState.value.concurrentRun.isComplete)
    }

    @Test
    fun `child task failure becomes visible lab state`() = runTest {
        val viewModel = viewModel(
            historyRepository = ColdBuildHistoryRepository(pauseBetweenStages = {}),
            reportReader = BuildReportReader {
                flow { throw IllegalStateException("security scanner offline") }
            },
        )

        viewModel.startConcurrentRun()
        advanceUntilIdle()

        assertEquals("security scanner offline", viewModel.uiState.value.concurrentRun.errorMessage)
        assertFalse(viewModel.uiState.value.concurrentRun.isRunning)
        assertFalse(viewModel.uiState.value.concurrentRun.isComplete)
    }

    private fun viewModel(
        historyRepository: ColdBuildHistoryRepository,
        reportReader: BuildReportReader = BuildReportReader { emptyFlow() },
    ): BuildPulseViewModel {
        val simulator = InMemoryBuildSimulator()
        return BuildPulseViewModel(
            statusReader = simulator,
            scenarioController = simulator,
            historyReader = historyRepository,
            reportReader = reportReader,
            buildId = BuildId("build-42"),
        )
    }
}
