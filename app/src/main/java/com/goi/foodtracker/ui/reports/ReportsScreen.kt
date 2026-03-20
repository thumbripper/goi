package com.goi.foodtracker.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.goi.foodtracker.data.db.dao.FoodCorrelation
import com.goi.foodtracker.data.db.entities.LogEntry
import com.goi.foodtracker.ui.main.FEELING_OPTIONS
import com.goi.foodtracker.ui.theme.FeelingBad
import com.goi.foodtracker.ui.theme.FeelingGood
import com.goi.foodtracker.ui.theme.FeelingNeutral
import com.goi.foodtracker.ui.theme.FeelingWorst
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Timeline", "Correlation", "Stats")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> TimelineTab(entries = state.timeline)
            1 -> CorrelationTab(correlations = state.correlations)
            2 -> StatsTab(
                topFoods = state.topFoods,
                avgScore = state.avgFeelingScore,
                correlations = state.correlations
            )
        }
    }
}

// ─── Timeline ────────────────────────────────────────────────────────────────

@Composable
private fun TimelineTab(entries: List<TimelineEntry>) {
    if (entries.isEmpty()) {
        EmptyState("No entries yet.\nStart logging from the Log tab!")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        items(entries) { item ->
            TimelineEntryCard(entry = item.entry)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TimelineEntryCard(entry: LogEntry) {
    val dateFormat = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())
    val emoji = entry.feelingScore?.let { score ->
        FEELING_OPTIONS.firstOrNull { it.first == score }?.second
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.feelingScore != null) {
                    Text(
                        text = "Feeling: $emoji (${feelingLabel(entry.feelingScore)})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (emoji != null) {
                Text(text = emoji, fontSize = 28.sp)
            }
        }
    }
}

private fun feelingLabel(score: Int) = when (score) {
    1 -> "Good"
    2 -> "Neutral"
    3 -> "Nauseous"
    4 -> "Sick"
    else -> "?"
}

// ─── Correlation ─────────────────────────────────────────────────────────────

@Composable
private fun CorrelationTab(correlations: List<FoodCorrelation>) {
    if (correlations.isEmpty()) {
        EmptyState("Not enough data yet.\nLog food with a feeling score to see correlations.")
        return
    }
    val maxScore = correlations.maxOfOrNull { it.avgScore }?.coerceAtLeast(1.0) ?: 4.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Food vs Symptom Correlation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Higher bar = more associated with bad symptoms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        items(correlations) { item ->
            CorrelationRow(item = item, maxScore = maxScore)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun CorrelationRow(item: FoodCorrelation, maxScore: Double) {
    val fraction = (item.avgScore / 4.0).toFloat().coerceIn(0f, 1f)
    val barColor = scoreColor(item.avgScore)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.foodName, fontWeight = FontWeight.SemiBold)
                Text(
                    "avg %.1f (n=%d)".format(item.avgScore, item.entryCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun scoreColor(avg: Double): Color = when {
    avg < 1.75 -> FeelingGood
    avg < 2.5 -> FeelingNeutral
    avg < 3.25 -> FeelingBad
    else -> FeelingWorst
}

// ─── Stats ───────────────────────────────────────────────────────────────────

@Composable
private fun StatsTab(
    topFoods: List<com.goi.foodtracker.data.db.dao.FoodFrequency>,
    avgScore: Double?,
    correlations: List<FoodCorrelation>
) {
    val bestFood = correlations.minByOrNull { it.avgScore }
    val worstFood = correlations.maxByOrNull { it.avgScore }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Average Feeling Score",
                value = avgScore?.let { "%.1f / 4.0  %s".format(it, scoreEmoji(it)) } ?: "No data"
            )
        }
        item {
            StatCard(
                title = "Best Correlated Food",
                value = bestFood?.let { "${it.foodName}  (avg ${it.avgScore.format1()})" } ?: "Not enough data"
            )
        }
        item {
            StatCard(
                title = "Worst Correlated Food",
                value = worstFood?.let { "${it.foodName}  (avg ${it.avgScore.format1()})" } ?: "Not enough data"
            )
        }
        if (topFoods.isNotEmpty()) {
            item {
                Text("Most Logged Foods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(topFoods) { food ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(food.foodName)
                    Text("${food.count}x", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun scoreEmoji(avg: Double) = when {
    avg < 1.75 -> "😊"
    avg < 2.5 -> "😐"
    avg < 3.25 -> "🤢"
    else -> "🤮"
}

private fun Double.format1() = "%.1f".format(this)
