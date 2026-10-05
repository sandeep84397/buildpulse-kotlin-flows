package io.github.buildpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.buildpulse.dashboard.BuildPulseApp
import io.github.buildpulse.dashboard.BuildPulseViewModel
import io.github.buildpulse.model.BuildId
import io.github.buildpulse.model.BuildTask
import io.github.buildpulse.simulation.CallbackFlowBuildHistoryRepository
import io.github.buildpulse.simulation.ChannelFlowBuildReportRepository
import io.github.buildpulse.simulation.InMemoryBuildSimulator
import io.github.buildpulse.simulation.SimulatedBuildTaskRunner

class MainActivity : ComponentActivity() {
    private val simulator = InMemoryBuildSimulator()
    private val historyRepository = CallbackFlowBuildHistoryRepository(simulator)
    private val reportRepository = ChannelFlowBuildReportRepository(
        runners = listOf(
            SimulatedBuildTaskRunner(BuildTask.COMPILER, startDelayMillis = 0, workDurationMillis = 900),
            SimulatedBuildTaskRunner(BuildTask.TESTS, startDelayMillis = 200, workDurationMillis = 1_100),
            SimulatedBuildTaskRunner(BuildTask.SECURITY_SCAN, startDelayMillis = 400, workDurationMillis = 800),
        ),
    )

    private val buildPulseViewModel by viewModels<BuildPulseViewModel> {
        viewModelFactory {
            initializer {
                BuildPulseViewModel(
                    statusReader = simulator,
                    scenarioController = simulator,
                    historyReader = historyRepository,
                    reportReader = reportRepository,
                    buildId = BuildId("build-42"),
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuildPulseApp(viewModel = buildPulseViewModel)
        }
    }
}
