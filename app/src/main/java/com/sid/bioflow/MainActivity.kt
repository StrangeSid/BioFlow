package com.sid.bioflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BioFlowApp()
        }
    }
}

// Define the screens as an enum
enum class Screen {
    Home, Health, Chat, Settings, SOS
}

@Composable
fun BioFlowApp() {
    // State to hold the currently selected screen
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            BioFlowBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenChange = { screen -> currentScreen = screen }
            )
        }
    ) { innerPadding ->
        // Use a Column to hold the current screen's content
        Column(modifier = Modifier.padding(innerPadding)) {
            // Use a when statement to show the correct screen
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Health -> HealthScreen()
                Screen.Chat -> ChatScreen()
                Screen.Settings -> SettingsScreen()
                Screen.SOS -> SosScreen()
            }
        }
    }
}