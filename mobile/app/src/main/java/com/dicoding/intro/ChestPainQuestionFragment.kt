package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChestPainQuestionFragment : Fragment(R.layout.fragment_chest_pain_question) {

    private lateinit var appDataStore: AppDataStore
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button
    private lateinit var backButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        // Inisialisasi tombol
        btnYes = view.findViewById(R.id.btn_yes)
        btnNo = view.findViewById(R.id.btn_no)
        backButton = view.findViewById(R.id.btn_back)

        // Menangani klik tombol Yes
        btnYes.setOnClickListener {
            handleChestPainSelection(-1, R.id.action_chestPainQuestionFragment_to_chestPainSliderFragment)
        }

        // Menangani klik tombol No
        btnNo.setOnClickListener {
            handleChestPainSelection(0, R.id.action_chestPainQuestionFragment_to_restingBpmFragment)
        }

        // Tombol Kembali
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleChestPainSelection(chestPainLevel: Int, actionId: Int) {
        lifecycleScope.launch {
            // Ambil data pengguna saat ini
            val userInput = appDataStore.userInputFlow.first()

            // Simpan data pengguna dengan nilai chestPainLevel yang baru
            appDataStore.saveUserInput(
                gender = userInput.gender,
                age = userInput.age,
                chestPainLevel = chestPainLevel,
                restingBpm = userInput.restingBpm,
                activityBpm = userInput.activityBpm,
                chestTightness = userInput.chestTightness,
                date = userInput.date
            )

            // Lakukan navigasi ke fragment yang sesuai
            findNavController().navigate(actionId)
        }
    }
}