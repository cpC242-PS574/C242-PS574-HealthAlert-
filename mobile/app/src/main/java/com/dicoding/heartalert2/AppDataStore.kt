package com.dicoding.heartalert2

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "user_inputs")

class AppDataStore(private val context: Context) {

    companion object {
        // Keys for storing user input and prediction results
        val GENDER_KEY = intPreferencesKey("gender")
        val AGE_KEY = intPreferencesKey("age")
        val CHEST_PAIN_LEVEL_KEY = intPreferencesKey("chestPainLevel")
        val RESTING_BPM_KEY = intPreferencesKey("restingBpm")
        val ACTIVITY_BPM_KEY = intPreferencesKey("activityBpm")
        val CHEST_TIGHTNESS_KEY = intPreferencesKey("chestTightness")
        val DATE_KEY = stringPreferencesKey("date")
        val PREDICTION_RESULT_KEY = stringPreferencesKey("predictionResult")
        val HISTORY_KEY = stringPreferencesKey("history") // Key for history list
    }

    // Cache the last saved input to avoid redundant saves
    private var lastSavedUserInput: UserInput? = null

    // Save user input
    suspend fun saveUserInput(
        gender: Int? = null,
        age: Int? = null,
        chestPainLevel: Int? = null,
        restingBpm: Int? = null,
        activityBpm: Int? = null,
        chestTightness: Int? = null,
        date: String? = null
    ) {
        val currentData = userInputFlow.first()

        // Merge new data with existing data
        val newUserInput = UserInput(
            gender = gender ?: currentData.gender,
            age = age ?: currentData.age,
            chestPainLevel = chestPainLevel ?: currentData.chestPainLevel,
            restingBpm = restingBpm ?: currentData.restingBpm,
            activityBpm = activityBpm ?: currentData.activityBpm,
            chestTightness = chestTightness ?: currentData.chestTightness,
            date = date ?: currentData.date
        )

        // Avoid redundant saves
        if (newUserInput == lastSavedUserInput) return

        // Save new data to DataStore
        context.dataStore.edit { preferences ->
            preferences[GENDER_KEY] = newUserInput.gender
            preferences[AGE_KEY] = newUserInput.age
            preferences[CHEST_PAIN_LEVEL_KEY] = newUserInput.chestPainLevel
            preferences[RESTING_BPM_KEY] = newUserInput.restingBpm
            preferences[ACTIVITY_BPM_KEY] = newUserInput.activityBpm
            preferences[CHEST_TIGHTNESS_KEY] = newUserInput.chestTightness
            preferences[DATE_KEY] = newUserInput.date
        }

        // Update cache
        lastSavedUserInput = newUserInput
    }

    // Save prediction result
    suspend fun savePredictionResult(prediction: Double) {
        context.dataStore.edit { preferences ->
            preferences[PREDICTION_RESULT_KEY] = prediction.toString()
        }
    }

    // Save a new history entry
    suspend fun saveHistoryEntry(entry: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[HISTORY_KEY]?.split(";")?.toMutableList() ?: mutableListOf()
            currentHistory.add(entry)
            preferences[HISTORY_KEY] = currentHistory.joinToString(";")
        }
    }

    // Flow to get user input data
    val userInputFlow: Flow<UserInput> = context.dataStore.data.map { preferences ->
        UserInput(
            gender = preferences[GENDER_KEY] ?: -1,
            age = preferences[AGE_KEY] ?: 16,
            chestPainLevel = preferences[CHEST_PAIN_LEVEL_KEY] ?: -1,
            restingBpm = preferences[RESTING_BPM_KEY] ?: -1,
            activityBpm = preferences[ACTIVITY_BPM_KEY] ?: -1,
            chestTightness = preferences[CHEST_TIGHTNESS_KEY] ?: -1,
            date = preferences[DATE_KEY] ?: ""
        )
    }

    // Flow to get prediction result
    val predictionResultFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PREDICTION_RESULT_KEY]?.toDoubleOrNull() ?: 0.0
    }

    // Flow to get history list
    val historyFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[HISTORY_KEY]?.split(";") ?: emptyList()
    }

    // Combine user input and prediction into a single Flow
    val combinedFlow: Flow<Pair<UserInput, Double>> = userInputFlow.combine(predictionResultFlow) { userInput, prediction ->
        Pair(userInput, prediction)
    }
}

// Data class for user input
data class UserInput(
    val gender: Int,
    val age: Int,
    val chestPainLevel: Int,
    val restingBpm: Int,
    val activityBpm: Int,
    val chestTightness: Int,
    val date: String
)