package com.sid.bioflow

// imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// sos screen composable
@Composable
fun SosScreen() {
    // sos screen UI elements
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emergency", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("SOS", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Emergency Contacts")
        listOf("Contact 1", "Contact 2", "Contact 3").forEach {
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(it)
            }
        }
    }
}