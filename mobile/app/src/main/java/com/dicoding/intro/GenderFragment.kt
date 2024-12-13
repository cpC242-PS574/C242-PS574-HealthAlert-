package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.AppDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GenderFragment : Fragment(R.layout.fragment_gender) {
    private lateinit var appDataStore: AppDataStore
    private lateinit var maleButton: Button
    private lateinit var femaleButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        maleButton = view.findViewById(R.id.btn_male)
        femaleButton = view.findViewById(R.id.btn_female)
        nextButton = view.findViewById(R.id.btn_next)
        backButton = view.findViewById(R.id.btn_back)

        // Tombol Next mulai dalam kondisi tidak aktif
        nextButton.isEnabled = false

        // Menangani klik tombol Male dan Female
        maleButton.setOnClickListener {
            selectGenderAndNavigate(1)
        }

        femaleButton.setOnClickListener {
            selectGenderAndNavigate(0)
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun selectGenderAndNavigate(gender: Int) {
        lifecycleScope.launch {
            // Simpan jenis kelamin yang dipilih ke dalam DataStore
            val userInput = appDataStore.userInputFlow.first() // Ambil data sekali
            appDataStore.saveUserInput(
                gender = gender,
                age = userInput.age,
                chestPainLevel = userInput.chestPainLevel,
                restingBpm = userInput.restingBpm,
                activityBpm = userInput.activityBpm,
                chestTightness = userInput.chestTightness,
                date = userInput.date
            )
        }
        // Navigasi ke fragment berikutnya setelah memilih gender
        findNavController().navigate(R.id.action_genderFragment_to_ageFragment)
    }
}