package com.sid.bioflow

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Ensure context is not null
        context?.let {
            // Create an intent to open the MainActivity when the notification is tapped
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0, // Request code
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
            )

            // Build the notification
            val builder = NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
                .setContentTitle("BioFlow Reminder") // Notification title
                .setContentText("Don't forget to log your Health...") // Notification text
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priority level
                .setContentIntent(pendingIntent) // Set the intent to be launched when the user clicks the notification
                .setAutoCancel(true) // Automatically removes the notification when the user taps it

            // Get the NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Show the notification
            // Use a unique ID for the notification, e.g., a constant or a timestamp
            val notificationId = 1 // Example notification ID
            notificationManager.notify(notificationId, builder.build())
        }
    }
}
