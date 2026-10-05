package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildReport
import io.github.buildpulse.model.BuildReportState
import io.github.buildpulse.model.BuildTask
import kotlinx.coroutines.delay

class SimulatedBuildTaskRunner(
    private val task: BuildTask,
    private val startDelayMillis: Long,
    private val workDurationMillis: Long,
) : BuildTaskRunner {

    override suspend fun run(
        buildId: BuildId,
        report: suspend (BuildReport) -> Unit,
    ) {
        delay(startDelayMillis)
        report(BuildReport(buildId, task, BuildReportState.STARTED))
        delay(workDurationMillis)
        report(BuildReport(buildId, task, BuildReportState.COMPLETED))
    }
}
