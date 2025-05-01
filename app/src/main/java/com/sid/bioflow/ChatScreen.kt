package com.sid.bioflow

// imports
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// chat screen composable
@Composable
fun ChatScreen() {
    // chat screen UI elements
    var input by remember { mutableStateOf("") } //remember state

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("AI Health Chat")
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(8.dp)) {
            Text("👩‍⚕️ BioFlow AI: How can I help you today?")
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your question...") }
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}