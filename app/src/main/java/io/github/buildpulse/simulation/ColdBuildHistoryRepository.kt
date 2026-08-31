package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import io.github.buildpulse.model.BuildStage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColdBuildHistoryRepository(
    private val pauseBetweenStages: suspend () -> Unit = { delay(DEFAULT_STAGE_DELAY_MILLIS) },
    private val onExecutionStarted: () -> Unit = {},
) : BuildHistoryReader {

    override fun observeHistory(buildId: BuildId): Flow<BuildSnapshot> = flow {
        onExecutionStarted()
        BuildStage.entries.forEachIndexed { index, stage ->
            emit(
                BuildSnapshot(
                    buildId = buildId,
                    stage = stage,
                    sequence = index,
                ),
            )
            if (stage != BuildStage.SUCCEEDED) {
                pauseBetweenStages()
            }
        }
    }

    private companion object {
        const val DEFAULT_STAGE_DELAY_MILLIS = 700L
    }
}
