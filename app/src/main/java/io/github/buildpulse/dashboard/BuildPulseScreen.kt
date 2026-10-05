package io.github.buildpulse.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.buildpulse.model.BuildStage
import io.github.buildpulse.model.BuildReportState
import io.github.buildpulse.model.BuildTask

private val BuildPulseColors = darkColorScheme(
    primary = Color(0xFF83E8C7),
    onPrimary = Color(0xFF00382C),
    secondary = Color(0xFF7CC4FF),
    background = Color(0xFF08111F),
    surface = Color(0xFF101C2D),
    surfaceVariant = Color(0xFF17263A),
    onSurface = Color(0xFFE6EDF7),
    onSurfaceVariant = Color(0xFFABB9CB),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BuildPulseApp(viewModel: BuildPulseViewModel) {
    MaterialTheme(colorScheme = BuildPulseColors) {
        Surface(modifier = Modifier.fillMaxSize()) {
            BuildPulseScreen(
                state = viewModel.uiState.value,
                onAdvanceServer = viewModel::advanceServer,
                onFetchSnapshot = viewModel::fetchSnapshot,
                onStartCollector = viewModel::startCollector,
                onStopCollector = viewModel::stopCollector,
                onStartConcurrentRun = viewModel::startConcurrentRun,
                onStopConcurrentRun = viewModel::stopConcurrentRun,
            )
        }
    }
}

@Composable
fun BuildPulseScreen(
    state: BuildPulseUiState,
    onAdvanceServer: () -> Unit,
    onFetchSnapshot: () -> Unit,
    onStartCollector: (CollectorId) -> Unit,
    onStopCollector: (CollectorId) -> Unit,
    onStartConcurrentRun: () -> Unit,
    onStopConcurrentRun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Header() }
        item { PreviousExperiment() }
        item { ChannelFlowProblem() }
        item {
            ChannelFlowLab(
                timeline = state.concurrentRun,
                onStart = onStartConcurrentRun,
                onStop = onStopConcurrentRun,
            )
        }
        item { LearningRoadmap() }
        item {
            Text(
                text = "Article 4 stops here. Next: understand why hot streams keep existing without a collector.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "BUILDPULSE / ARTICLE 04",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.6.sp,
        )
        Text(
            text = "Three producers.\nOne Flow.",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
        )
        Text(
            text = "channelFlow lets concurrent child coroutines send compiler, test, and security reports into one cold stream.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PreviousExperiment() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(
            text = "ARTICLE 03 RECAP: callbackFlow owns one listener lifetime. Article 04 needs several suspending producers to report concurrently.",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Composable
private fun ChannelFlowProblem() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "THE ARTICLE 04 PROBLEM",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = "Compiler, tests, and security scan run at the same time. A regular flow builder expects one sequential emitter.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "channelFlow launches structured children. Each child uses send(); one collector receives reports by arrival.",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChannelFlowLab(
    timeline: ConcurrentRunTimeline,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "RUN THE CONCURRENT WORKSHOP",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
        )
        Text(
            text = "One collect() starts all three child producers. Watch their reports merge into the collector timeline.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        BuildTask.entries.forEach { task ->
            val state = timeline.reports.lastOrNull { it.task == task }?.state
            TaskLane(task = task, state = state)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "MERGED COLLECTOR TIMELINE",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.1.sp,
                )
                Text(
                    text = when {
                        timeline.errorMessage != null -> "Run ${timeline.runNumber}: child failure cancelled the workshop"
                        timeline.isRunning -> "Run ${timeline.runNumber}: collecting"
                        timeline.isComplete -> "Run ${timeline.runNumber}: all children completed"
                        timeline.runNumber > 0 -> "Run ${timeline.runNumber}: cancelled"
                        else -> "No collection. No child producer has started."
                    },
                    color = if (timeline.errorMessage == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (timeline.reports.isEmpty()) {
                    Text(
                        text = "Reports appear here in arrival order.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    timeline.reports.forEachIndexed { index, report ->
                        Text(
                            text = "${index + 1}. ${report.task.label} → ${report.state.label}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                timeline.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (timeline.runNumber == 0) "Start collection" else "Run again")
            }
            OutlinedButton(
                onClick = onStop,
                enabled = timeline.isRunning,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun TaskLane(
    task: BuildTask,
    state: BuildReportState?,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = task.label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = state?.label ?: "WAITING",
                color = when (state) {
                    BuildReportState.STARTED -> MaterialTheme.colorScheme.secondary
                    BuildReportState.COMPLETED -> MaterialTheme.colorScheme.primary
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CallbackFlowProblem() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "THE ARTICLE 03 PROBLEM",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = "The CI SDK owns a listener API: addListener() sends future build updates, but our UI wants a cancellable Flow.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "collect() registers. trySend() forwards. awaitClose removes the listener.",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CallbackFlowLab(
    state: BuildPulseUiState,
    onStartCollector: (CollectorId) -> Unit,
    onStopCollector: (CollectorId) -> Unit,
) {
    val activeListeners = listOf(state.collectorA, state.collectorB).count { it.isRunning }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "BRIDGE THE CALLBACK SDK",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
        )
        Text(
            text = "Start a collector, then tap Advance server above. The SDK calls every registered listener; callbackFlow forwards that update to its collector.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Active collector-owned listeners: $activeListeners",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
        CollectorCard(
            collectorId = CollectorId.A,
            timeline = state.collectorA,
            accent = MaterialTheme.colorScheme.primary,
            onStart = onStartCollector,
            onStop = onStopCollector,
        )
        CollectorCard(
            collectorId = CollectorId.B,
            timeline = state.collectorB,
            accent = MaterialTheme.colorScheme.secondary,
            onStart = onStartCollector,
            onStop = onStopCollector,
        )
    }
}

@Composable
private fun CollectorCard(
    collectorId: CollectorId,
    timeline: CollectorTimeline,
    accent: Color,
    onStart: (CollectorId) -> Unit,
    onStop: (CollectorId) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "COLLECTOR ${collectorId.name}",
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.1.sp,
                    )
                    Text(
                        text = when {
                            timeline.isRunning && timeline.snapshots.isEmpty() ->
                                "Collection ${timeline.collectionNumber}: listener registered"
                            timeline.isRunning ->
                                "Collection ${timeline.collectionNumber}: listening"
                            timeline.isComplete ->
                                "Collection ${timeline.collectionNumber} execution complete"
                            timeline.collectionNumber > 0 ->
                                "Collection ${timeline.collectionNumber} stopped"
                            else -> "Idle — no listener registered"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (timeline.isRunning) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                        ),
                )
            }

            if (timeline.snapshots.isEmpty()) {
                Text(
                    text = if (timeline.isRunning) {
                        "Waiting for the SDK callback. Past updates are not replayed."
                    } else {
                        "No values. Start collecting to register the listener."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                timeline.snapshots.forEach { snapshot ->
                    Text(
                        text = "${snapshot.sequence + 1}. ${snapshot.stage.label}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { onStart(collectorId) },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (timeline.collectionNumber == 0) "Start listening" else "Listen again")
                }
                OutlinedButton(
                    onClick = { onStop(collectorId) },
                    enabled = timeline.isRunning,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun StatusComparison(
    state: BuildPulseUiState,
    onAdvanceServer: () -> Unit,
    onFetchSnapshot: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(
                title = "SERVER NOW",
                stage = state.serverStage.label,
                sequence = state.serverSequence,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                title = "UI SNAPSHOT",
                stage = state.fetchedStage?.label ?: "NOT FETCHED",
                sequence = state.fetchedSequence,
                accent = if (state.isSnapshotStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }

        if (state.isSnapshotStale) {
            Text(
                text = "Snapshot is stale: the server changed, but no new value reached the UI.",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onAdvanceServer,
                enabled = !state.isWorking && state.serverStage != BuildStage.SUCCEEDED,
                modifier = Modifier.weight(1f),
            ) {
                Text("Advance server", textAlign = TextAlign.Center)
            }
            Button(
                onClick = onFetchSnapshot,
                enabled = !state.isWorking,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
            ) {
                Text("Fetch snapshot", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    stage: String,
    sequence: Int?,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape),
            )
            Text(
                text = stage,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = sequence?.let { "Revision $it" } ?: "No response yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PipelineProgress(currentStage: BuildStage) {
    val stages = BuildStage.entries
    val currentIndex = stages.indexOf(currentStage)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "THE CHANGING SYSTEM",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(14.dp))
            stages.forEachIndexed { index, stage ->
                val symbol = when {
                    index < currentIndex -> "✓"
                    index == currentIndex -> "●"
                    else -> "○"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = symbol,
                        color = if (index <= currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = stage.label,
                        color = if (index == currentIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningRoadmap() {
    val steps = listOf(
        "01  One-time result — complete",
        "02  Cold Flow — complete",
        "03  callbackFlow — complete",
        "04  channelFlow — you are here",
        "05  Hot streams",
        "06  StateFlow and SharedFlow",
        "07  stateIn and shareIn",
        "08  Channel",
        "09  Complete decision guide",
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "LEARNING ROADMAP",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                Text(
                    text = step,
                    color = if (index == 3) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == 3) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 7.dp),
                )
                if (index < steps.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

private val BuildStage.label: String
    get() = name.replace('_', ' ')

private val BuildTask.label: String
    get() = when (this) {
        BuildTask.COMPILER -> "COMPILER"
        BuildTask.TESTS -> "TEST RUNNER"
        BuildTask.SECURITY_SCAN -> "SECURITY SCANNER"
    }

private val BuildReportState.label: String
    get() = when (this) {
        BuildReportState.STARTED -> "RUNNING"
        BuildReportState.COMPLETED -> "COMPLETE"
    }

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun BuildPulsePreview() {
    MaterialTheme(colorScheme = BuildPulseColors) {
        BuildPulseScreen(
            state = BuildPulseUiState(
                serverStage = BuildStage.RUNNING_TESTS,
                serverSequence = 2,
                fetchedStage = BuildStage.COMPILING,
                fetchedSequence = 1,
            ),
            onAdvanceServer = {},
            onFetchSnapshot = {},
            onStartCollector = {},
            onStopCollector = {},
            onStartConcurrentRun = {},
            onStopConcurrentRun = {},
        )
    }
}
