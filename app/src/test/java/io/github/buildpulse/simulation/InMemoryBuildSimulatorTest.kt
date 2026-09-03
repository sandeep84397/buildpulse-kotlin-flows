package io.github.buildpulse.simulation

import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.BuildSnapshot
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

    @Test
    fun `registered listener receives advances until it is removed`() = runTest {
        val buildId = BuildId("build-42")
        val simulator = InMemoryBuildSimulator()
        val updates = mutableListOf<BuildSnapshot>()
        val listener = BuildUpdateListener(updates::add)

        simulator.addListener(listener)
        assertEquals(emptyList<BuildSnapshot>(), updates)

        simulator.advanceServer(buildId)
        assertEquals(listOf(BuildStage.COMPILING), updates.map { it.stage })

        simulator.removeListener(listener)
        simulator.advanceServer(buildId)

        assertEquals(listOf(BuildStage.COMPILING), updates.map { it.stage })
    }

    @Test
    fun `listener is not called again when terminal build cannot advance`() = runTest {
        val buildId = BuildId("build-42")
        val simulator = InMemoryBuildSimulator()
        val updates = mutableListOf<BuildSnapshot>()
        simulator.addListener(BuildUpdateListener(updates::add))

        repeat(BuildStage.entries.lastIndex) {
            simulator.advanceServer(buildId)
        }
        assertEquals(BuildStage.SUCCEEDED, updates.last().stage)
        val updatesAtTerminal = updates.size

        simulator.advanceServer(buildId)

        assertEquals(updatesAtTerminal, updates.size)
    }
}
