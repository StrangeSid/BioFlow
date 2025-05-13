package com.sid.bioflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.content.Context // Import Context


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenUI(
    // State parameters received from the parent
    hcEnabled: Boolean,
    notifEnabled: Boolean,
    aiEnabled: Boolean,
    // Callbacks to signal state changes to the parent
    onHcToggle: (Boolean) -> Unit,
    onNotifToggle: (Boolean) -> Unit,
    onAiToggle: (Boolean) -> Unit,
    onRemoveAllData: () -> Unit,
    onAskAiHelp: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Use the passed-in state and callbacks
        SwitchSetting(label = "Use Health Connect", state = hcEnabled, onToggle = onHcToggle)
        SwitchSetting(label = "Notifications", state = notifEnabled, onToggle = onNotifToggle)
        SwitchSetting(label = "Allow AI to use Data", state = aiEnabled, onToggle = onAiToggle)

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRemoveAllData, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Remove All Data / Reset")
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Use the passed-in callback for the text button
        TextButton(onClick = { onAskAiHelp() }) { Text("Need Help? Ask the AI") }
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

// Modify ViewModel to accept NotificationScheduler
class SettingsViewModel(
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    // Use StateFlow for observable state that other components can collect
    private val _hcEnabled = MutableStateFlow(true)
    val hcEnabled: StateFlow<Boolean> = _hcEnabled.asStateFlow()

    private val _notifEnabled = MutableStateFlow(true)
    val notifEnabled: StateFlow<Boolean> = _notifEnabled.asStateFlow()

    private val _aiEnabled = MutableStateFlow(true)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled.asStateFlow()

    // Functions to update the state and interact with the scheduler
    fun toggleHcEnabled(enabled: Boolean) {
        _hcEnabled.value = !_hcEnabled.value
        println("HC Enabled: ${_hcEnabled.value}")
    }

    fun toggleNotifEnabled(enabled: Boolean) {
        _notifEnabled.value = !_notifEnabled.value
        println("Notif Enabled: ${_notifEnabled.value}")

        // Schedule or cancel the notification based on the new state
        if (_notifEnabled.value) {
            notificationScheduler.scheduleDailyReminder()
        } else {
            notificationScheduler.cancelDailyReminder()
        }
    }

    fun toggleAiEnabled(enabled: Boolean) {
        _aiEnabled.value = !_aiEnabled.value
        println("AI Enabled: ${_aiEnabled.value}")
    }

    fun removeAllData() {
        _hcEnabled.value = true
        _notifEnabled.value = true
        _aiEnabled.value = true
        // Also cancel notifications when data is removed/reset
        notificationScheduler.cancelDailyReminder()
    }

    // Companion object for ViewModel factory
    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Create and provide the NotificationScheduler instance
                SettingsViewModel(notificationScheduler = NotificationScheduler(context.applicationContext))
            }
        }
    }
}

@Composable
fun SettingsScreen( // A composable that uses the ViewModel and calls the UI
    settingsViewModel: SettingsViewModel = viewModel(), // ViewModel will be provided by the factory
    onScreenChange: (Screen) -> Unit
) {
    // Observe the state from the ViewModel using collectAsState()
    val hcEnabled by settingsViewModel.hcEnabled.collectAsState()
    val notifEnabled by settingsViewModel.notifEnabled.collectAsState()
    val aiEnabled by settingsViewModel.aiEnabled.collectAsState()
    // Pass the state and ViewModel functions down to the UI composable
    SettingsScreenUI(
        hcEnabled = hcEnabled,
        notifEnabled = notifEnabled,
        aiEnabled = aiEnabled,
        onHcToggle = settingsViewModel::toggleHcEnabled, // Pass ViewModel function references
        onNotifToggle = settingsViewModel::toggleNotifEnabled,
        onAiToggle = settingsViewModel::toggleAiEnabled,
        onRemoveAllData = settingsViewModel::removeAllData,
        onAskAiHelp = {onScreenChange(Screen.Chat)},
    )
}
