package com.dicoding.heartalert2

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPreferencesHelper(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)

    fun getString(key: String): String? {
        return preferences.getString(key, "")
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    // Save measurement result with date as key
    fun saveMeasurementResult(date: String, result: String) {
        if (result.contains(",")) { // Validate format before saving
            preferences.edit().putString("result_$date", result).apply()
        } else {
            Log.e("SharedPreferencesHelper", "Invalid result format: $result")
        }
    }

    // Retrieve all measurement results
    fun getAllMeasurements(): Map<String, String> {
        return preferences.all.filterKeys { it.startsWith("result_") }
            .mapKeys { it.key.removePrefix("result_") }
            .mapValues { it.value as String }
    }
}