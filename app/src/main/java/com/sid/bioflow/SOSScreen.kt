package com.sid.bioflow

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun SosScreen(onScreenChange: (Screen) -> Unit) {
    val context = LocalContext.current
    // Use a mutable list to hold the contact numbers
    var contactList by remember { mutableStateOf(listOf("Emergency Contact #1", "Emergency Contact #2", "Emergency Contact #3")) }

    var dialogVisible by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var newNumber by remember { mutableStateOf(TextFieldValue("")) }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { Text("Edit Contact") },
            text = {
                TextField(
                    value = newNumber,
                    onValueChange = { newNumber = it },
                    label = { Text("Phone Number") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNumber.text.isNotBlank()) {
                        // Update the contact list
                        contactList = contactList.toMutableList().apply {
                            if (editingIndex != -1) {
                                this[editingIndex] = newNumber.text
                            }
                        }
                        dialogVisible = false
                        newNumber = TextFieldValue("") // Reset the input field
                    } else {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emergency", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:112")
                context.startActivity(intent)
            },
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            border = BorderStroke(5.dp, Color.Red),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("SOS", fontSize = 50.sp, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Emergency Contacts")

        contactList.forEachIndexed { index, number ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = number, modifier = Modifier.weight(1f))

                    Row {
                        IconButton(onClick = {
                            newNumber = TextFieldValue(contactList[index])
                            editingIndex = index
                            dialogVisible = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:${contactList[index]}")
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                    }
                }
            }
        }
    }
}