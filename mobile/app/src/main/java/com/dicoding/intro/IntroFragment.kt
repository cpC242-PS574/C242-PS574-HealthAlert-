package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.dicoding.heartalert2.R
import androidx.navigation.fragment.findNavController
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dicoding.heartalert2.AppDataStore
import kotlinx.coroutines.launch

class IntroFragment : Fragment(R.layout.fragment_intro) {

    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        val startButton: Button = view.findViewById(R.id.btn_start)
        val lastResultCardView: CardView = view.findViewById(R.id.last_result_cardview)
        val lastResultTextView: TextView = view.findViewById(R.id.last_result_text)

        // Menampilkan riwayat pengukuran terakhir
        loadLastResult(lastResultTextView, lastResultCardView)

        // Navigate to GenderFragment when startButton is clicked
        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_genderFragment)
        }

        // Navigate to AllHistoryFragment when lastResultCardView is clicked
        lastResultCardView.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_allHistoryFragment)
        }
    }

    private fun loadLastResult(lastResultTextView: TextView, lastResultCardView: CardView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appDataStore.userInputFlow.collect { userInput ->
                    // Displaying the Activity BPM
                    val activityBpm = userInput.activityBpm

                    // Fetching the last prediction result
                    appDataStore.predictionResultFlow.collect { prediction ->
                        // Using the Elvis operator to handle null
                        val displayPrediction = prediction ?: 0.0
                        // Determining the risk status
                        val riskStatus = if (displayPrediction >= 0.5) "Beresiko!" else "Normal"

                        // Displaying the result in TextView
                        lastResultTextView.text = "$activityBpm BPM\nPrediksi: $riskStatus"

                        // Display the CardView if there is history
                        lastResultCardView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}