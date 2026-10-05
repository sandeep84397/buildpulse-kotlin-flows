package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildReport
import io.github.buildpulse.model.BuildReportState
import io.github.buildpulse.model.BuildTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelFlowBuildReportRepositoryTest {

    @Test
    fun `creating the Flow does not start any build task`() = runTest {
        var starts = 0
        val repository = ChannelFlowBuildReportRepository(
            runners = List(3) {
                BuildTaskRunner { _, _ -> starts++ }
            },
        )

        repository.observeReports(BuildId("build-42"))

        assertEquals(0, starts)
    }

    @Test
    fun `collecting runs every task concurrently and merges reports by arrival`() = runTest {
        val buildId = BuildId("build-42")
        val repository = ChannelFlowBuildReportRepository(
            runners = listOf(
                scheduledRunner(BuildTask.COMPILER, completionDelayMillis = 100),
                scheduledRunner(BuildTask.TESTS, completionDelayMillis = 200),
                scheduledRunner(BuildTask.SECURITY_SCAN, completionDelayMillis = 300),
            ),
        )

        val reports = repository.observeReports(buildId).toList()

        assertEquals(300, currentTime)
        assertEquals(
            listOf(BuildTask.COMPILER, BuildTask.TESTS, BuildTask.SECURITY_SCAN),
            reports.filter { it.state == BuildReportState.COMPLETED }.map { it.task },
        )
        assertEquals(3, reports.count { it.state == BuildReportState.STARTED })
        assertEquals(3, reports.count { it.state == BuildReportState.COMPLETED })
        assertTrue(reports.all { it.buildId == buildId })
    }

    @Test
    fun `each collector starts a fresh execution of every task`() = runTest {
        val starts = mutableMapOf<BuildTask, Int>()
        val runners = BuildTask.entries.map { task ->
            BuildTaskRunner { buildId, report ->
                starts[task] = starts.getOrDefault(task, 0) + 1
                report(BuildReport(buildId, task, BuildReportState.COMPLETED))
            }
        }
        val repository = ChannelFlowBuildReportRepository(runners)
        val reports = repository.observeReports(BuildId("build-42"))

        reports.toList()
        reports.toList()

        assertEquals(
            mapOf(
                BuildTask.COMPILER to 2,
                BuildTask.TESTS to 2,
                BuildTask.SECURITY_SCAN to 2,
            ),
            starts,
        )
    }

    @Test
    fun `cancelling collection cancels every running child task`() = runTest {
        var cancellations = 0
        val runners = BuildTask.entries.map { task ->
            BuildTaskRunner { buildId, report ->
                report(BuildReport(buildId, task, BuildReportState.STARTED))
                try {
                    awaitCancellation()
                } finally {
                    cancellations++
                }
            }
        }
        val repository = ChannelFlowBuildReportRepository(runners)
        val received = mutableListOf<BuildReport>()
        val collection = launch {
            repository.observeReports(BuildId("build-42")).collect(received::add)
        }
        runCurrent()

        collection.cancelAndJoin()

        assertEquals(3, received.size)
        assertEquals(3, cancellations)
    }

    @Test
    fun `failure in one child task cancels its siblings and fails collection`() = runTest {
        var siblingCancellations = 0
        val failingRunner = BuildTaskRunner { _, _ ->
            yield()
            throw IllegalStateException("security scanner offline")
        }
        val waitingRunner = BuildTaskRunner { _, _ ->
            try {
                awaitCancellation()
            } finally {
                siblingCancellations++
            }
        }
        val repository = ChannelFlowBuildReportRepository(
            runners = listOf(waitingRunner, failingRunner, waitingRunner),
        )

        val failure = runCatching {
            repository.observeReports(BuildId("build-42")).toList()
        }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
        assertEquals("security scanner offline", failure?.message)
        assertEquals(2, siblingCancellations)
    }

    private fun scheduledRunner(
        task: BuildTask,
        completionDelayMillis: Long,
    ) = BuildTaskRunner { buildId, report ->
        report(BuildReport(buildId, task, BuildReportState.STARTED))
        delay(completionDelayMillis)
        report(BuildReport(buildId, task, BuildReportState.COMPLETED))
    }
}
