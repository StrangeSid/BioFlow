package com.sid.bioflow

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

@Composable
fun SosScreen() {
    Text("SOS Page", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
}