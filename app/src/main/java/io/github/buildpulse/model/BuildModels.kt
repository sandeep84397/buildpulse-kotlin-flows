package io.github.buildpulse.model

@JvmInline
value class BuildId(val value: String)

enum class BuildStage {
    QUEUED,
    COMPILING,
    RUNNING_TESTS,
    SECURITY_SCAN,
    DEPLOYING,
    SUCCEEDED,
}

fun BuildStage.next(): BuildStage = when (this) {
    BuildStage.QUEUED -> BuildStage.COMPILING
    BuildStage.COMPILING -> BuildStage.RUNNING_TESTS
    BuildStage.RUNNING_TESTS -> BuildStage.SECURITY_SCAN
    BuildStage.SECURITY_SCAN -> BuildStage.DEPLOYING
    BuildStage.DEPLOYING -> BuildStage.SUCCEEDED
    BuildStage.SUCCEEDED -> BuildStage.SUCCEEDED
}

data class BuildSnapshot(
    val buildId: BuildId,
    val stage: BuildStage,
    val sequence: Int,
)

enum class BuildTask {
    COMPILER,
    TESTS,
    SECURITY_SCAN,
}

enum class BuildReportState {
    STARTED,
    COMPLETED,
}

data class BuildReport(
    val buildId: BuildId,
    val task: BuildTask,
    val state: BuildReportState,
)
