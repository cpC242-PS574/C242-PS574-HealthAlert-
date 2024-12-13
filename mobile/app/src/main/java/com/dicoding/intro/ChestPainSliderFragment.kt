package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChestPainSliderFragment : Fragment(R.layout.fragment_chest_pain_slider) {
    private lateinit var appDataStore: AppDataStore

    private lateinit var nextButton: Button
    private lateinit var slider: Slider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        nextButton = view.findViewById(R.id.btn_next)
        slider = view.findViewById(R.id.slider_chest_pain)

        // Disable next button initially
        nextButton.isEnabled = false

        // Enable next button only if the user interacts with the slider
        slider.addOnChangeListener { slider, value, fromUser ->
            nextButton.isEnabled = true
        }

        nextButton.setOnClickListener {
            val chestPainLevel = slider.value.toInt() // 1 to 3
            // Save chest pain level to DataStore
            lifecycleScope.launch {
                val userInput = appDataStore.userInputFlow.first()
                appDataStore.saveUserInput(
                    gender = userInput.gender,
                    age = userInput.age,
                    chestPainLevel = chestPainLevel,
                    restingBpm = userInput.restingBpm,
                    activityBpm = userInput.activityBpm,
                    chestTightness = userInput.chestTightness,
                    date = userInput.date
                )
            }
            findNavController().navigate(R.id.action_chestPainSliderFragment_to_restingBpmFragment)
        }
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
