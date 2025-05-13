package com.sid.bioflow

// imports
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras // Import CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory // Import viewModelFactory


internal lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


class MainActivity : ComponentActivity() {

    // run main app composable when started
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Call createNotificationChannels and pass the activity context
        createNotificationChannels(this)

        // We will now schedule/cancel the alarm via the SettingsViewModel
        // Remove the direct call to scheduleDailyReminder(this) here


        setContent {
            // Get the SettingsViewModel using the factory
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(this.applicationContext) // Pass application context to the factory
            )
            BioFlowApp(settingsViewModel = settingsViewModel)
        }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }

            if (allGranted) {
                println("All Health Connect permissions granted!")
            } else {
                println("Not all Health Connect permissions were granted.")
            }
        }
    }
}

// add screens as enum
enum class Screen {
    Home, Health, Chat, Settings, SOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioFlowApp(
    // Receive the ViewModel instance
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current.applicationContext) // Use LocalContext to get context in Composable
    )
) {
    // State for the currently selected screen, hoisted here
    var selectedScreen by remember { mutableStateOf(Screen.Home) }

    // Lambda function to update the selected screen state
    val navigateToScreen: (Screen) -> Unit = { screen ->
        selectedScreen = screen
    }

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
                    onClick = { navigateToScreen(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Health,
                    onClick = { navigateToScreen(Screen.Health) },
                    icon = { Icon(Icons.Default.Medication, null) },
                    label = { Text("Health") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Chat,
                    onClick = { navigateToScreen(Screen.Chat) },
                    icon = { Icon(Icons.Default.Send, null) },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Settings,
                    onClick = { navigateToScreen(Screen.Settings) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.SOS,
                    onClick = { navigateToScreen(Screen.SOS) },
                    icon = { Icon(Icons.Default.Warning, null) },
                    label = { Text("SOS") }
                )
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen(
                    onScreenChange = navigateToScreen,
                    settingsViewModel = settingsViewModel
                )
                Screen.Health -> HealthScreen(
                    onScreenChange = navigateToScreen,
                    settingsViewModel = settingsViewModel
                )
                Screen.Chat -> ChatScreen(
                    onScreenChange = navigateToScreen,
                    settingsViewModel = settingsViewModel
                )
                Screen.Settings -> SettingsScreen(
                    onScreenChange = navigateToScreen,
                    settingsViewModel = settingsViewModel // Pass the ViewModel instance
                )
                Screen.SOS -> SosScreen(
                    onScreenChange = navigateToScreen
                )
            }
        }
    }
}

const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_channel"

// Modified function to accept a Context parameter
private fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Data Log Reminders"
        val descriptionText = "Channel for daily data logging reminders"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val reminderChannel = NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        // Use the passed context to get the system service
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.createNotificationChannel(reminderChannel)
    }
}
