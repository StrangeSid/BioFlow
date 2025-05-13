package com.sid.bioflow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

fun scheduleDailyReminder(context: Context, settingsViewModel: SettingsViewModel) {

    // Get the AlarmManager system service
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Create an Intent to trigger the ReminderBroadcastReceiver
    val intent = Intent(context, ReminderBroadcastReceiver::class.java)

    // Create a PendingIntent for the broadcast
    // Use FLAG_IMMUTABLE for security reasons on newer Android versions
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0, // Request code
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
    )

    // Set the time for 9 PM
    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 21) // 21 is 9 PM
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    // If the current time is already past 9 PM, schedule for the next day
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Schedule the repeating alarm
    // Use setInexactRepeating for battery efficiency for daily alarms
    // RTC_WAKEUP wakes up the device to fire the alarm at the specified time
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP, // Alarm type
        calendar.timeInMillis, // Start time
        AlarmManager.INTERVAL_DAY, // Repeat interval (daily)
        pendingIntent // The PendingIntent to be fired
    )
}
