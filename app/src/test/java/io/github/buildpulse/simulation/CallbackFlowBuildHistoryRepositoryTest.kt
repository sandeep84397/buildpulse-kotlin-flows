package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildSnapshot
import io.github.buildpulse.model.BuildStage
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallbackFlowBuildHistoryRepositoryTest {

    @Test
    fun `creating the Flow does not register a callback listener`() = runTest {
        val sdk = RecordingBuildCallbackSdk()
        val repository = CallbackFlowBuildHistoryRepository(sdk)

        repository.observeHistory(BuildId("build-42"))

        assertEquals(0, sdk.listenerCount)
    }

    @Test
    fun `collecting registers a listener and forwards matching callbacks in order`() = runTest {
        val buildId = BuildId("build-42")
        val sdk = RecordingBuildCallbackSdk()
        val repository = CallbackFlowBuildHistoryRepository(sdk)
        val received = mutableListOf<BuildSnapshot>()
        val collection = launch {
            repository.observeHistory(buildId).take(2).toList(received)
        }
        runCurrent()

        sdk.publish(snapshot(buildId, BuildStage.COMPILING, 1))
        sdk.publish(snapshot(buildId, BuildStage.RUNNING_TESTS, 2))
        collection.join()

        assertEquals(
            listOf(BuildStage.COMPILING, BuildStage.RUNNING_TESTS),
            received.map { it.stage },
        )
        assertEquals(0, sdk.listenerCount)
    }

    @Test
    fun `callbacks for another build are ignored`() = runTest {
        val buildId = BuildId("build-42")
        val sdk = RecordingBuildCallbackSdk()
        val repository = CallbackFlowBuildHistoryRepository(sdk)
        val received = mutableListOf<BuildSnapshot>()
        val collection = launch {
            repository.observeHistory(buildId).take(1).toList(received)
        }
        runCurrent()

        sdk.publish(snapshot(BuildId("build-99"), BuildStage.COMPILING, 1))
        sdk.publish(snapshot(buildId, BuildStage.RUNNING_TESTS, 2))
        collection.join()

        assertEquals(listOf(BuildStage.RUNNING_TESTS), received.map { it.stage })
    }

    @Test
    fun `cancelling collection unregisters its listener and blocks later callbacks`() = runTest {
        val buildId = BuildId("build-42")
        val sdk = RecordingBuildCallbackSdk()
        val repository = CallbackFlowBuildHistoryRepository(sdk)
        val received = mutableListOf<BuildSnapshot>()
        val collection = launch {
            repository.observeHistory(buildId).collect(received::add)
        }
        runCurrent()

        assertEquals(1, sdk.listenerCount)
        sdk.publish(snapshot(buildId, BuildStage.COMPILING, 1))
        runCurrent()

        collection.cancelAndJoin()
        assertEquals(0, sdk.listenerCount)

        sdk.publish(snapshot(buildId, BuildStage.RUNNING_TESTS, 2))
        runCurrent()

        assertEquals(listOf(BuildStage.COMPILING), received.map { it.stage })
    }

    @Test
    fun `each collector owns one listener until its collection is cancelled`() = runTest {
        val sdk = RecordingBuildCallbackSdk()
        val repository = CallbackFlowBuildHistoryRepository(sdk)
        val updates = repository.observeHistory(BuildId("build-42"))
        val collectorA = launch { updates.collect() }
        runCurrent()
        assertEquals(1, sdk.listenerCount)

        val collectorB = launch { updates.collect() }
        runCurrent()
        assertEquals(2, sdk.listenerCount)

        collectorA.cancelAndJoin()
        assertEquals(1, sdk.listenerCount)

        collectorB.cancelAndJoin()
        assertEquals(0, sdk.listenerCount)
    }

    private fun snapshot(
        buildId: BuildId,
        stage: BuildStage,
        sequence: Int,
    ) = BuildSnapshot(
        buildId = buildId,
        stage = stage,
        sequence = sequence,
    )

    private class RecordingBuildCallbackSdk : BuildCallbackSource {
        private val listeners = linkedSetOf<BuildUpdateListener>()

        val listenerCount: Int
            get() = listeners.size

        override fun addListener(listener: BuildUpdateListener) {
            listeners += listener
        }

        override fun removeListener(listener: BuildUpdateListener) {
            listeners -= listener
        }

        fun publish(snapshot: BuildSnapshot) {
            listeners.toList().forEach { it.onBuildUpdated(snapshot) }
        }
    }
}
