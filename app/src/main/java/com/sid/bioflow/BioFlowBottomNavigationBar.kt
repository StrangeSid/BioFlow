package com.sid.bioflow

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning

@Composable
fun BioFlowBottomNavigationBar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    NavigationBar {
        // Use a list of pairs for screen data
        val items = listOf(
            Screen.Home to "Home",
            Screen.Health to "Health",
            Screen.Chat to "Chat",
            Screen.Settings to "Settings",
            Screen.SOS to "SOS"
        )

        items.forEach { (screen, label) ->
            NavigationBarItem(
                label = { Text(label) },
                selected = currentScreen == screen,
                onClick = { onScreenChange(screen) },
                icon = {
                    Icon(imageVector = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Health -> Icons.Filled.Favorite
                        Screen.Chat -> Icons.Filled.Send
                        Screen.Settings -> Icons.Filled.Settings
                        Screen.SOS -> Icons.Filled.Warning
                    }, contentDescription = label)
                }
            )
        }
    }
}