package com.sid.bioflow

// imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// settings screen composable
@Composable
fun SettingsScreen() {
    var hcEnabled by remember { mutableStateOf(true) } // remember setting state
    var notifEnabled by remember { mutableStateOf(true) } // remember setting state
    var aiEnabled by remember { mutableStateOf(true) } // remember setting state
    // settings screen UI elements
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings")

        Spacer(modifier = Modifier.height(8.dp))
        Text("Data & Privacy")
        SwitchSetting("Use Health Connect", hcEnabled) { hcEnabled = it }
        SwitchSetting("Notifications", notifEnabled) { notifEnabled = it }
        SwitchSetting("Allow AI to use Data", aiEnabled) { aiEnabled = it }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Remove All Data / Reset")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {}) { Text("Need Help? Ask the AI") }
    }
}
// switch setting composable function
@Composable
fun SwitchSetting(label: String, state: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = state, onCheckedChange = onToggle)
    }
}