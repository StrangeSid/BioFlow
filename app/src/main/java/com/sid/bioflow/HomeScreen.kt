package com.sid.bioflow

// imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// home screen composable
@Composable
fun HomeScreen() {
    // home screen UI elements
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Hello, User 👋", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Health Metrics")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(modifier = Modifier.weight(1f).padding(4.dp)) {
                Text("HR: 72", modifier = Modifier.padding(8.dp))
            }
            Card(modifier = Modifier.weight(1f).padding(4.dp)) {
                Text("BP: 120/80", modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Health Graph (Placeholder)")
        Card(modifier = Modifier.fillMaxWidth().height(120.dp).padding(4.dp)) {}

        Spacer(modifier = Modifier.height(16.dp))
        Text("AI Insight: You’re doing great!")
    }
}