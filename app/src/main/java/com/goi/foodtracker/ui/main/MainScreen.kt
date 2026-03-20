package com.goi.foodtracker.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val customFoods by viewModel.customFoods.collectAsState()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            delay(1500)
            viewModel.clearSaveSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("How are you feeling?", style = MaterialTheme.typography.titleLarge)

        // Emoji feeling row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FEELING_OPTIONS.forEach { (score, emoji) ->
                val selected = state.feelingScore == score
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setFeelingScore(score) },
                    label = {
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                )
            }
        }

        Text("What did you eat or drink?", style = MaterialTheme.typography.titleLarge)

        // Preset food chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PRESET_FOODS.forEach { (name, emoji) ->
                FilterChip(
                    selected = name in state.selectedFoods,
                    onClick = { viewModel.toggleFood(name) },
                    label = { Text("$emoji $name") }
                )
            }
        }

        // Saved custom favourites (if any)
        if (customFoods.isNotEmpty()) {
            Text("Favourites", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                customFoods.forEach { item ->
                    FilterChip(
                        selected = item.name in state.selectedFoods,
                        onClick = { viewModel.toggleFood(item.name) },
                        label = { Text(item.name) }
                    )
                }
            }
        }

        // Custom text entry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.customText,
                onValueChange = { viewModel.setCustomText(it) },
                label = { Text("Add custom item") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { viewModel.addCustomTextToSelection() },
                enabled = state.customText.isNotBlank()
            ) {
                Text("Add")
            }
        }

        // Save as favourite checkbox — only shown when custom text is being typed or custom item is selected
        val hasCustomSelection = state.selectedFoods.any { food ->
            PRESET_FOODS.none { it.first == food } && customFoods.none { it.name == food }
        }
        AnimatedVisibility(visible = hasCustomSelection) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.saveCustom,
                    onCheckedChange = { viewModel.setSaveCustom(it) }
                )
                Text("Save new items as favourites")
            }
        }

        // Selected foods summary
        if (state.selectedFoods.isNotEmpty()) {
            Text(
                text = "Selected: ${state.selectedFoods.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.saveEntry() },
            enabled = state.selectedFoods.isNotEmpty() || state.feelingScore != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Entry", style = MaterialTheme.typography.titleMedium)
        }

        // Success snackbar
        AnimatedVisibility(visible = state.saveSuccess) {
            Snackbar {
                Text("Entry saved!")
            }
        }
    }
}
