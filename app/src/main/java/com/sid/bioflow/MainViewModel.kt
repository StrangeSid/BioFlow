package com.sid.bioflow

import android.app.Application
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.time.LocalDate // Import LocalDate
import java.time.LocalTime // Import LocalTime
import java.time.ZoneOffset // Import ZoneOffset

// Defines a DataStore instance for the application.
val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "health_score_persistence")
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore // Access DataStore via application context

    val healthConnectClient = mutableStateOf<HealthConnectClient?>(null)
    var hasAllPermissions = mutableStateOf(false)
        private set

    internal val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    internal val _scoreHistory = MutableStateFlow<List<ScoreDataPoint>>(emptyList())
    val scoreHistory: StateFlow<List<ScoreDataPoint>> = _scoreHistory.asStateFlow()

    companion object {
        // Key for storing score history in DataStore
        private val SCORE_HISTORY_KEY = stringPreferencesKey("score_history_json_data")
    }

    init {
        loadScoreHistory() // Load saved scores when ViewModel is created
    }

    private fun loadScoreHistory() {
        viewModelScope.launch(Dispatchers.IO) { // Perform DataStore operations on IO dispatcher
            try {
                val jsonString = dataStore.data.map { preferences ->
                    preferences[SCORE_HISTORY_KEY]
                }.firstOrNull()

                if (!jsonString.isNullOrBlank()) {
                    val history = Json.decodeFromString<ScoreHistory>(jsonString)
                    _scoreHistory.value = history.points.sortedBy { it.timestamp }
                }
            } catch (e: Exception) {
                // This catch is important for debugging serialization issues during load
                println("Error deserializing score history on load: ${e.message}")
                _toastEvents.emit("Failed to load score history.")
                e.printStackTrace() // Print full stack trace for detailed debugging
            }
        }
    }

    internal suspend fun saveScoreHistory(points: List<ScoreDataPoint>) {
        try {
            val history = ScoreHistory(points)
            val jsonString = Json.encodeToString(history)
            dataStore.edit { preferences ->
                preferences[SCORE_HISTORY_KEY] = jsonString
            }
        } catch (e: Exception) {
            println("Error serializing score history in saveScoreHistory: ${e.message}")
            _toastEvents.emit("Error saving score data.")
            e.printStackTrace() // Print full stack trace! This is key to debugging serialization.
        }
    }

    fun logDataAndGetScore(dataToLog: Map<String, String>, entryTimestamp: Instant) {
        viewModelScope.launch(Dispatchers.IO) { // Ensure LLM call & DataStore are off the main thread
            try {
                val score =
                    getScore(dataToLog.toString()) // Call your (placeholder or real) LLM function
                val newScorePoint =
                    ScoreDataPoint(timestamp = entryTimestamp, score = score.toFloat())
                // Update the history
                val currentHistory = _scoreHistory.value
                val updatedHistory = (currentHistory + newScorePoint).sortedBy { it.timestamp }

                _scoreHistory.value = updatedHistory // Update UI immediately
                saveScoreHistory(updatedHistory)   // Persist

                saveDataToHealthConnect(dataToLog, entryTimestamp, requestPermissionLauncher)

                _toastEvents.emit("Score logged: ${"%.1f".format(score)}")

            } catch (e: Exception) {
                println("Error in logDataAndGetScore: ${e.message}")
                _toastEvents.emit("Error processing or scoring data.")
                e.printStackTrace()
            }
        }
    }

    // --- Health Connect Related Functions ---
    private val PERMISSIONS = setOf(
        HealthPermission.getWritePermission(HeightRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun initializeHealthConnectClient(client: HealthConnectClient?) {
        healthConnectClient.value = client
        if (client != null) {
            checkPermissions()
        }
    }

    fun MainViewModel.checkPermissions() {
        viewModelScope.launch {
            healthConnectClient.value?.let { client ->
                val granted = client.permissionController.getGrantedPermissions()
                hasAllPermissions.value = granted.containsAll(PERMISSIONS)
                if (!hasAllPermissions.value) {
                    println("Not all Health Connect permissions are granted.")
                } else {
                    println("All Health Connect permissions are granted.")
                }
            }
        }
    }

    fun MainViewModel.requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (!hasAllPermissions.value) {
            permissionLauncher.launch(PERMISSIONS.toTypedArray())
        }
    }

    // Example function to save data to Health Connect, now using Metadata.manualEntry
    fun MainViewModel.saveDataToHealthConnect(
        dataMap: Map<String, String>, // Accept the map as input
        entryTimestamp: Instant,
        permissionLauncher: ActivityResultLauncher<Array<String>> // Pass the launcher
    ) {
        // Request permissions before attempting to save
        requestPermissions(permissionLauncher)
        // Launch coroutine to handle the data saving asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            if (healthConnectClient.value == null) {
                _toastEvents.emit("Health Connect client not initialized.")
                return@launch // Exit coroutine if client is null
            }
            if (!hasAllPermissions.value) {
                _toastEvents.emit("Health Connect permissions not granted. Please grant permissions to save data.")
                return@launch
            }

            val recordsToInsert = mutableListOf<Record>()
            val currentZoneOffset = ZoneId.systemDefault().rules.getOffset(entryTimestamp)

            // Extract data from the map and create records
            // Use get() which returns nullable if the key is not present
            dataMap["height_m"]?.toDoubleOrNull()?.let { heightCm ->
                // Original code expected input in cm and converted to meters
                recordsToInsert.add(
                    HeightRecord(
                        entryTimestamp,
                        currentZoneOffset,
                        Length.meters(heightCm / 100.0),
                        Metadata.manualEntry("height_${UUID.randomUUID()}")
                    )
                )
            }

            // Weight (assuming value in map is in kilograms based on key "weight_kg")
            dataMap["weight_kg"]?.toDoubleOrNull()?.let { weightKg ->
                recordsToInsert.add(
                    WeightRecord(
                        entryTimestamp,
                        currentZoneOffset,
                        Mass.kilograms(weightKg),
                        Metadata.manualEntry("weight_${UUID.randomUUID()}")
                    )
                )
            }
            // Hydration (assuming value in map is in milliliters based on key "hydration_ml")
            dataMap["hydration_ml"]?.toDoubleOrNull()?.let { hydrationMl ->
                recordsToInsert.add(
                    HydrationRecord(
                        entryTimestamp.minusSeconds(1),
                        currentZoneOffset,
                        entryTimestamp,
                        currentZoneOffset,
                        Volume.milliliters(hydrationMl),
                        Metadata.manualEntry("hydration_${UUID.randomUUID()}")
                    )
                )
            }
            // Sleep Duration (assuming value in map is in hours based on key "sleep_duration_hours")
            dataMap["sleep_duration_hours"]?.toDoubleOrNull()?.let { durationHours ->
                val endTime = entryTimestamp
                // Convert hours to milliseconds for duration calculation
                val durationMillis = (durationHours * 3600 * 1000).toLong()
                val startTime = endTime.minusMillis(durationMillis)
                // Retrieve the note text from the map
                val noteText = dataMap["note"]
                recordsToInsert.add(
                    SleepSessionRecord(
                        startTime,
                        currentZoneOffset,
                        endTime,
                        currentZoneOffset,
                        title = "Nightly Sleep",
                        notes = noteText,
                        metadata = Metadata.manualEntry("sleep_${UUID.randomUUID()}")
                    )
                )
            }

            if (recordsToInsert.isNotEmpty()) {
                try {
                    val response = healthConnectClient.value?.insertRecords(recordsToInsert)
                    // You can inspect the response if needed, e.g., response.recordIdsList
                    _toastEvents.emit("Raw data successfully saved to Health Connect.")
                } catch (e: Exception) {
                    _toastEvents.emit("Error saving data to Health Connect: ${e.message}")
                    println("Health Connect insertion error: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                _toastEvents.emit("No valid data found in the map to save.")
            }
        }
    }

    // Function to reset the score history
    fun resetScoreHistory() {
        viewModelScope.launch(Dispatchers.IO) { // Perform DataStore operation off main thread
            try {
                // Update the StateFlow to an empty list
                _scoreHistory.value = emptyList()
                // Save an empty ScoreHistory to DataStore
                saveScoreHistory(emptyList())
                _toastEvents.emit("Score history reset.")
            } catch (e: Exception) {
                println("Error resetting score history: ${e.message}")
                _toastEvents.emit("Failed to reset score history.")
                e.printStackTrace()
            }
        }
    }

    private val _averageSleepHours = MutableStateFlow<Double?>(null)
    val averageSleepHours: StateFlow<Double?> = _averageSleepHours.asStateFlow()

    /**
     * Reads sleep session records from Health Connect for a given time range and calculates the average sleep duration.
     * Updates the averageSleepHours StateFlow.
     *
     * @param startTime The start of the time range (Instant).
     * @param endTime The end of the time range (Instant).
     */
    suspend fun loadAverageSleepHours(startTime: Instant, endTime: Instant) { // Renamed to indicate loading
        if (healthConnectClient.value == null) {
            _toastEvents.emit("Health Connect client not initialized.")
            _averageSleepHours.value = null // Reset state on error
            return
        }

        if (!hasAllPermissions.value) {
            _toastEvents.emit("Health Connect permissions not granted. Cannot load sleep data.")
            _averageSleepHours.value = null // Reset state if permissions are missing
            return
        }


        try {
            val response = healthConnectClient.value?.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val sleepSessions = response?.records ?: emptyList()

            if (sleepSessions.isEmpty()) {
                _toastEvents.emit("No sleep data found for the selected period.")
                _averageSleepHours.value = 0.0 // Or null, depending on how you want to represent no data
                Log.d("MainViewModel", "No sleep data found. Setting average to 0.0")
                return
            }

            var totalDurationMillis = 0L
            for (session in sleepSessions) {
                val duration = java.time.Duration.between(session.startTime, session.endTime)
                totalDurationMillis += duration.toMillis()
            }

            val averageDurationMillis = totalDurationMillis / sleepSessions.size
            val averageDurationHours = averageDurationMillis.toDouble() / (1000 * 60 * 60) // Convert milliseconds to hours

            _averageSleepHours.value = averageDurationHours // Update the StateFlow
            Log.d("MainViewModel", "Calculated average sleep: %.2f hours from ${sleepSessions.size} sessions. Updated StateFlow.".format(averageDurationHours))

        } catch (e: Exception) {
            _toastEvents.emit("Error reading sleep data from Health Connect: ${e.message}")
            Log.e("MainViewModel", "Error reading sleep data", e)
            _averageSleepHours.value = null // Reset state on error
        }
    }
    fun fetchAverageSleepLast30Days() {
        viewModelScope.launch {
            val endTime = Instant.now()
            val startTime = endTime.minus(30, ChronoUnit.DAYS)
            loadAverageSleepHours(startTime, endTime)
        }
    }

    init {
        loadScoreHistory()
        fetchAverageSleepLast30Days()
    }

    private val _averageDailyScore = MutableStateFlow<Float?>(null)
    val averageDailyScore: StateFlow<Float?> = _averageDailyScore.asStateFlow()
    fun calculateAverageScoreForDay(date: LocalDate) {
        viewModelScope.launch(Dispatchers.Default) {
            val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
            val endOfDay = date.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)

            val scoresForDay = _scoreHistory.value.filter { scoreDataPoint ->
                val scoreInstant = scoreDataPoint.timestamp
                scoreInstant.isAfter(startOfDay) && scoreInstant.isBefore(endOfDay) || scoreInstant == startOfDay || scoreInstant == endOfDay
            }

            if (scoresForDay.isNotEmpty()) {
                val average = scoresForDay.map { it.score }.average().toFloat()
                _averageDailyScore.value = average
                Log.d("MainViewModel", "Calculated average score for $date: $average")
            } else {
                _averageDailyScore.value = null
                Log.d("MainViewModel", "No scores found for $date. Setting average to null.")
            }
        }
    }
}


