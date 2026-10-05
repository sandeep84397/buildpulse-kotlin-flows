package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildReport
import io.github.buildpulse.model.BuildReportState
import io.github.buildpulse.model.BuildTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedBuildTaskRunnerTest {

    @Test
    fun `runner reports started then completed using its configured timing`() = runTest {
        val reports = mutableListOf<BuildReport>()
        val runner = SimulatedBuildTaskRunner(
            task = BuildTask.SECURITY_SCAN,
            startDelayMillis = 100,
            workDurationMillis = 200,
        )

        runner.run(BuildId("build-42"), reports::add)

        assertEquals(300, currentTime)
        assertEquals(
            listOf(BuildReportState.STARTED, BuildReportState.COMPLETED),
            reports.map { it.state },
        )
        assertEquals(listOf(BuildTask.SECURITY_SCAN, BuildTask.SECURITY_SCAN), reports.map { it.task })
        assertEquals(listOf(BuildId("build-42"), BuildId("build-42")), reports.map { it.buildId })
    }
}
