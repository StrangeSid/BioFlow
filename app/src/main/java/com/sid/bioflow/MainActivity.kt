package com.sid.bioflow

// imports
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    // run main app composable when started
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BioFlowApp()
        }
    }
}
// add screens as enum
enum class Screen {
    Home, Health, Chat, Settings, SOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioFlowApp() {
    var selectedScreen by remember { mutableStateOf(Screen.Home) } // remember the selected screen

    Scaffold(
        // top bar
        topBar = {
            TopAppBar(
                title = { Text("BioFlow", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.DarkGray),
            )
        },
        // bottom bar
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == Screen.Home,
                    onClick = { selectedScreen = Screen.Home },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Health,
                    onClick = { selectedScreen = Screen.Health },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Health") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Chat,
                    onClick = { selectedScreen = Screen.Chat },
                    icon = { Icon(Icons.Default.Send, null) },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Settings,
                    onClick = { selectedScreen = Screen.Settings },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.SOS,
                    onClick = { selectedScreen = Screen.SOS },
                    icon = { Icon(Icons.Default.Warning, null) },
                    label = { Text("SOS") }
                )
            }
        }
    ) { innerPadding ->
        // link each enum to the composable to load the screen contents
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen()
                Screen.Health -> HealthScreen()
                Screen.Chat -> ChatScreen()
                Screen.Settings -> SettingsScreen()
                Screen.SOS -> SosScreen()
            }
        }
    }
}