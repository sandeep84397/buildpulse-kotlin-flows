package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildStage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryBuildSimulatorTest {

    @Test
    fun `advancing server does not mutate an earlier fetched snapshot`() = runTest {
        val buildId = BuildId("build-42")
        val simulator = InMemoryBuildSimulator()

        val fetchedSnapshot = simulator.fetchStatus(buildId)
        val advancedServer = simulator.advanceServer(buildId)

        assertEquals(BuildStage.QUEUED, fetchedSnapshot.stage)
        assertEquals(0, fetchedSnapshot.sequence)
        assertEquals(BuildStage.COMPILING, advancedServer.stage)
        assertEquals(1, advancedServer.sequence)
        assertEquals(BuildStage.COMPILING, simulator.currentServerStatus(buildId).stage)
    }
}
