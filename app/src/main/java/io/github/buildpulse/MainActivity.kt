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
import io.github.buildpulse.simulation.ColdBuildHistoryRepository
import io.github.buildpulse.simulation.InMemoryBuildSimulator

class MainActivity : ComponentActivity() {
    private val simulator = InMemoryBuildSimulator()
    private val historyRepository = ColdBuildHistoryRepository()

    private val buildPulseViewModel by viewModels<BuildPulseViewModel> {
        viewModelFactory {
            initializer {
                BuildPulseViewModel(
                    statusReader = simulator,
                    scenarioController = simulator,
                    historyReader = historyRepository,
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
