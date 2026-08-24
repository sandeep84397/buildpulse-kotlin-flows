package io.github.buildpulse.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildStageTest {

    @Test
    fun `next progresses through the pipeline and keeps success terminal`() {
        assertEquals(BuildStage.COMPILING, BuildStage.QUEUED.next())
        assertEquals(BuildStage.RUNNING_TESTS, BuildStage.COMPILING.next())
        assertEquals(BuildStage.SECURITY_SCAN, BuildStage.RUNNING_TESTS.next())
        assertEquals(BuildStage.DEPLOYING, BuildStage.SECURITY_SCAN.next())
        assertEquals(BuildStage.SUCCEEDED, BuildStage.DEPLOYING.next())
        assertEquals(BuildStage.SUCCEEDED, BuildStage.SUCCEEDED.next())
    }
}
