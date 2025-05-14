package com.sid.bioflow

// imports
import android.content.Context
import android.graphics.Color
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// chat screen
@Composable
fun ChatScreen(
    onScreenChange: (Screen) -> Unit,
    settingsViewModel: SettingsViewModel
) {

    val aiEnabled by settingsViewModel.aiEnabled.collectAsState()
    val context = LocalContext.current
    // URL to load
    val url = "http://10.0.2.2:8080/"
    // Open the URL in a Custom Tab
    if (aiEnabled) {
        openCustomTab(context, url)
        onScreenChange(Screen.Home)
    } else {
        Column() {
        Text("AI is not enabled", fontSize = 50.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Go to Settings to enable AI", fontSize = 20.sp, textAlign = TextAlign.Center)
    }
    }
}
fun openCustomTab(context: Context, url: String) {
    // Create a CustomTabsIntent builder
    val builder = CustomTabsIntent.Builder()
    // Set the toolbar color to white to match the website's background
    builder.setToolbarColor(Color.WHITE)
    val customTabsIntent = builder.build()
    // Launch the URL in the Custom Tab
    customTabsIntent.launchUrl(context, url.toUri())
}