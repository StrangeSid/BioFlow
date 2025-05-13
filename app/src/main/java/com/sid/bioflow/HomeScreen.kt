package com.sid.bioflow

// imports
import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.runBlocking

// home screen composable
@Composable
fun HomeScreen(
    onScreenChange: (Screen) -> Unit,
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val averageSleep by mainViewModel.averageSleepHours.collectAsState()
    val averageScore by mainViewModel.averageDailyScore.collectAsState()

    // home screen UI elements
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Hello, User 👋", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Health Metrics")

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // First Metric Card (Sleep)
            Card(
                modifier = Modifier
                    .border(shape = RoundedCornerShape(8.dp), width = 3.dp, color = Color.DarkGray)
                    .weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Average Sleep",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.DarkGray
                    )
                    Text(
                        text = "${averageSleep ?: "No Data"}",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Card(
                modifier = Modifier
                    .border(shape = RoundedCornerShape(8.dp), width = 3.dp, color = Color.DarkGray)
                    .weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Daily Score",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.DarkGray
                    )
                    Text(
                        text = "${averageScore?: "Log some Data!"}",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
        val scoreHistory by mainViewModel.scoreHistory.collectAsState()
        val hcEnabled by settingsViewModel.hcEnabled.collectAsState()
        val aiEnabled by settingsViewModel.aiEnabled.collectAsState()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Health Graph")
        if (scoreHistory.isNotEmpty()) {
            val sortedHistory = remember(scoreHistory) {
                scoreHistory.sortedBy { it.timestamp }
            }
            val yValues = remember(sortedHistory) {
                sortedHistory.map { it.score.toDouble() }
            }
            if (hcEnabled && aiEnabled) {
                LineChart(
                    modifier = Modifier
                        .height(150.dp)
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
            } else { Text("Features needed for this disabled", fontSize = 35.sp, textAlign = TextAlign.Center)
                Text("Please enable them in Settings.", fontSize = 20.sp, textAlign = TextAlign.Center) }
        } else {
            Text(
                "No score data yet.",
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(Modifier
            .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val insight = runBlocking { gemini(
                    "Prove a short single sentence insight for a general person\n" +
                    "It could even be an FAQ or a quote or a fun fact...\n" +
                    "Medical or Health Related.\n" +
                    "Short and sweet. and in make it sound human. Like a suggestion maybe or a tip?\n\n" +
                    "Don't include any formattion characters or symbols.\n" +
                    "Just plain text."
                ) }
                Text("Insight for you:", textAlign = TextAlign.Left, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("$insight", textAlign = TextAlign.Left, fontSize = 12.sp)
            }
        }

    }
}

