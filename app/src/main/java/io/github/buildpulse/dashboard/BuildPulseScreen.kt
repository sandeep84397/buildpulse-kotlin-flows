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
            )
        }
    }
}

@Composable
fun BuildPulseScreen(
    state: BuildPulseUiState,
    onAdvanceServer: () -> Unit,
    onFetchSnapshot: () -> Unit,
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
        item { ProblemStatement() }
        item {
            StatusComparison(
                state = state,
                onAdvanceServer = onAdvanceServer,
                onFetchSnapshot = onFetchSnapshot,
            )
        }
        item { PipelineProgress(currentStage = state.serverStage) }
        item { LearningRoadmap() }
        item {
            Text(
                text = "Article 1 stops here. The next article replaces repeated snapshots with a cold Flow.",
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
            text = "BUILDPULSE / ARTICLE 01",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.6.sp,
        )
        Text(
            text = "One answer.\nA changing system.",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
        )
        Text(
            text = "A suspend function can wait efficiently—but it still returns only one result.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ProblemStatement() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(
            text = "Experiment: fetch the build once, advance the server, then compare what the server knows with what the UI still displays.",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(18.dp),
        )
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
        "01  One-time result — you are here",
        "02  Cold Flow",
        "03  callbackFlow",
        "04  channelFlow",
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
                    color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
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
        )
    }
}
