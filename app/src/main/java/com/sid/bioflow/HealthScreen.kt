package com.sid.bioflow

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import android.widget.Toast
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.GridProperties
import java.time.LocalDate


class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onScreenChange: (Screen) -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val hcEnabled by settingsViewModel.hcEnabled.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        mainViewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val client = HealthConnectClient.getOrCreate(context)
            mainViewModel.initializeHealthConnectClient(client)
        } catch (e: Exception) {
            Toast.makeText(context, "Health Connect not available.", Toast.LENGTH_LONG).show()
            println(e)
        }
    }

    var height by remember { mutableStateOf("") }
    var hydration by remember { mutableStateOf("") }
    var sleepDuration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var noteInputText by remember { mutableStateOf("") }

    val scoreHistory by mainViewModel.scoreHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Health Logger & Score") })
        },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (scoreHistory.isNotEmpty()) {
                    val sortedHistory = remember(scoreHistory) {
                        scoreHistory.sortedBy { it.timestamp }
                    }
                    val yValues = remember(sortedHistory) {
                        sortedHistory.map { it.score.toDouble() }
                    }
                    if (hcEnabled) {
                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(vertical = 8.dp),
                            data = listOf(
                                Line(
                                    values = yValues,
                                    color = SolidColor(Color(0xFF00a100)), // Line color
                                    drawStyle = DrawStyle.Stroke(width = 2.dp), // Line style
                                    dotProperties = DotProperties( // Simple dots
                                        enabled = true,
                                        color = SolidColor(Color(0xFF0b8a0b)),
                                        radius = 3.dp
                                    ),
                                    label = "Health Score",
                                    curvedEdges = true,
                                    firstGradientFillColor = Color(0xFF61ff61).copy(alpha = .5f),
                                    secondGradientFillColor = Color.Transparent,
                                    gradientAnimationDelay = 500,

                                    )
                            ),
                            minValue = 0.0,
                            maxValue = 100.0,
                            animationMode = AnimationMode.Together(delayBuilder = { it * 50L }),
                            gridProperties = GridProperties(enabled = false)
                        )
                    } else { Text("Health Connect not enabled.", fontSize = 35.sp, textAlign = TextAlign.Center)
                        Text("Please enable Health Connect in Settings.", fontSize = 20.sp, textAlign = TextAlign.Center) }
                } else {
                    Text(
                        "No score data yet.", // Simplified message
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                CustomDropdownMenu(label = "Height (cm)", options = (120..220).map { it.toString() }, selectedValue = height, onSelected = { height = it })
                CustomDropdownMenu(label = "Water Intake (ml)", options = (250..5000 step 250).map { it.toString() }, selectedValue = hydration, onSelected = { hydration = it })
                CustomDropdownMenu(label = "Sleep Duration (hours)", options = (1..24).map { (it * 0.5).toString() }, selectedValue = sleepDuration, onSelected = { sleepDuration = it })
                CustomDropdownMenu(label = "Weight (kg)", options = (30..150 step 1).map { it.toString() }, selectedValue = weight, onSelected = { weight = it })
                OutlinedTextField(value = noteInputText, onValueChange = { noteInputText = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

                Button(
                    onClick = {
                        if (!hcEnabled) {
                            Toast.makeText(context, "Please enable Health Connect first.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val timestampForEntry = Instant.now()
                        val dataToLog = mutableMapOf<String, String>().apply {
                            if (height.isNotBlank()) this["height_m"] = height
                            if (hydration.isNotBlank()) this["hydration_ml"] = hydration
                            if (sleepDuration.isNotBlank()) this["sleep_duration_hours"] = sleepDuration
                            if (weight.isNotBlank()) this["weight_kg"] = weight
                            if (noteInputText.isNotBlank()) this["note"] = noteInputText
                        }
                        if (dataToLog.isNotEmpty()) {
                            mainViewModel.logDataAndGetScore(dataToLog, timestampForEntry)
                            height = ""; hydration = ""; sleepDuration = ""; weight = ""; noteInputText = ""
                            mainViewModel.calculateAverageScoreForDay(LocalDate.now())
                        } else {
                            Toast.makeText(context, "Please enter some data.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = height.isNotBlank() || hydration.isNotBlank() || sleepDuration.isNotBlank() || weight.isNotBlank() || noteInputText.isNotBlank()
                ) {
                    Text("Log Data & Get Score")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) { var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedValue,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    } }