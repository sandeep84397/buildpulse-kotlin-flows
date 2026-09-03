package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CallbackFlowBuildHistoryRepository(
    private val callbackSource: BuildCallbackSource,
) : BuildHistoryReader {

    override fun observeHistory(buildId: BuildId): Flow<BuildSnapshot> = callbackFlow {
        val listener = BuildUpdateListener { snapshot ->
            if (snapshot.buildId == buildId) {
                trySend(snapshot).onFailure {
                    // This low-rate learning lab deliberately drops updates after closure or overflow.
                }
            }
        }

        callbackSource.addListener(listener)
        awaitClose {
            callbackSource.removeListener(listener)
        }
    }
}
