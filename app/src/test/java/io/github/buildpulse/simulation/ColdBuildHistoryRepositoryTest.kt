package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildStage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ColdBuildHistoryRepositoryTest {

    private val expectedStages = listOf(
        BuildStage.QUEUED,
        BuildStage.COMPILING,
        BuildStage.RUNNING_TESTS,
        BuildStage.SECURITY_SCAN,
        BuildStage.DEPLOYING,
        BuildStage.SUCCEEDED,
    )

    @Test
    fun `defining history does not start an execution`() = runTest {
        var executionStarts = 0
        val repository = repository(onExecutionStarted = { executionStarts++ })

        repository.observeHistory(BuildId("build-42"))

        assertEquals(0, executionStarts)
    }

    @Test
    fun `collecting history emits every build stage in order`() = runTest {
        val buildId = BuildId("build-42")
        val repository = repository()

        val snapshots = repository.observeHistory(buildId).toList()

        assertEquals(expectedStages, snapshots.map { it.stage })
        assertEquals(listOf(0, 1, 2, 3, 4, 5), snapshots.map { it.sequence })
        assertEquals(List(expectedStages.size) { buildId }, snapshots.map { it.buildId })
    }

    @Test
    fun `each collector starts an independent execution`() = runTest {
        var executionStarts = 0
        val repository = repository(onExecutionStarted = { executionStarts++ })
        val history = repository.observeHistory(BuildId("build-42"))

        val collectorA = history.map { it.stage }.toList()
        val collectorB = history.map { it.stage }.toList()

        assertEquals(expectedStages, collectorA)
        assertEquals(expectedStages, collectorB)
        assertEquals(2, executionStarts)
    }

    @Test
    fun `stopping collection prevents later stages from being prepared`() = runTest {
        var pauses = 0
        val repository = repository(pauseBetweenStages = { pauses++ })

        val received = repository
            .observeHistory(BuildId("build-42"))
            .take(2)
            .toList()

        assertEquals(listOf(BuildStage.QUEUED, BuildStage.COMPILING), received.map { it.stage })
        assertEquals(1, pauses)
    }

    private fun repository(
        pauseBetweenStages: suspend () -> Unit = {},
        onExecutionStarted: () -> Unit = {},
    ) = ColdBuildHistoryRepository(
        pauseBetweenStages = pauseBetweenStages,
        onExecutionStarted = onExecutionStarted,
    )
}
