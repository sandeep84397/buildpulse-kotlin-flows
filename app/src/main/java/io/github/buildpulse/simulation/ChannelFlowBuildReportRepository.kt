package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class ChannelFlowBuildReportRepository(
    private val runners: List<BuildTaskRunner>,
) : BuildReportReader {

    override fun observeReports(buildId: BuildId): Flow<BuildReport> = channelFlow {
        runners.forEach { runner ->
            launch {
                runner.run(buildId) { report ->
                    send(report)
                }
            }
        }
    }
}
