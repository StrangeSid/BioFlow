package com.sid.bioflow

// imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// health screen composable
@Composable
fun HealthScreen() {
    // health screen UI elements
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Track Your Health", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Upload/Graph Placeholder")
        Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {}

        Spacer(modifier = Modifier.height(16.dp))
        Text("Select Category")
        var selected by remember { mutableStateOf("General") }
        DropdownMenuBox(selected, onSelect = { selected = it })

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Other Notes") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {}) {
            Text("Submit")
        }
    }
}

// dropdown menu composable
@Composable
fun DropdownMenuBox(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("General", "Sleep", "Diet", "Symptoms").forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}