package com.dicoding.intro

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.ml.ModelOptimized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChestTightnessFragment : Fragment(R.layout.fragment_chest_tightness) {

    private lateinit var finishButton: Button
    private lateinit var btnYes: Button
    private lateinit var btnNo: Button
    private lateinit var appDataStore: AppDataStore
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingOverlay: View
    private var chestTightness: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        // Inisialisasi View
        finishButton = view.findViewById(R.id.btn_finish)
        btnYes = view.findViewById(R.id.btn_yes)
        btnNo = view.findViewById(R.id.btn_no)
        progressBar = view.findViewById(R.id.progressBar)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        finishButton.isEnabled = false

        // Aksi saat tombol Yes ditekan
        btnYes.setOnClickListener {
            handleChestTightnessSelection(1)
        }

        // Aksi saat tombol No ditekan
        btnNo.setOnClickListener {
            handleChestTightnessSelection(0)
        }

        // Tombol Kembali
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleChestTightnessSelection(tightness: Int) {
        chestTightness = tightness
        val currentDate = SimpleDateFormat("dd-MMM-yy HH:mm:ss", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            // Tampilkan loading
            showLoading(true)

            // Simpan data ke DataStore
            val userInput = appDataStore.userInputFlow.first()
            appDataStore.saveUserInput(
                gender = userInput.gender,
                age = userInput.age,
                chestPainLevel = userInput.chestPainLevel,
                restingBpm = userInput.restingBpm,
                activityBpm = userInput.activityBpm,
                chestTightness = tightness,
                date = currentDate
            )

            // Siapkan data untuk dikirim
            val input = listOf(
                userInput.age,
                userInput.gender,
                userInput.chestPainLevel,
                userInput.restingBpm,
                userInput.activityBpm,
                tightness
            )

            // Kirim data ke endpoint
            val prediction = getPrediction(input)

            // Sembunyikan loading
            showLoading(false)

            if (prediction != null) {
                appDataStore.savePredictionResult(prediction)
                findNavController().navigate(R.id.action_chestTightnessFragment_to_resultFragment)
            } else {
//                Toast.makeText(requireContext(), "Gagal memproses prediksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getPrediction(input: List<Int>): Double? {
        return sendPredictionRequestWithRetry(input)
    }

    private suspend fun sendPredictionRequestWithRetry(input: List<Int>, maxRetries: Int = 3): Double? {
        var attempt = 0
        while (attempt < maxRetries) {
            val result = sendPredictionRequest(input)
            if (result != null) return result
            attempt++
            delay(2000L)
        }
        return null
    }

    private suspend fun sendPredictionRequest(input: List<Int>): Double? {
        val url = getString(R.string.modelMl)
        val jsonBody = JSONObject().apply { put("input", JSONArray(input)) }

        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder().url(url).post(requestBody).build()

                val response = client.newCall(request).execute()
                response.use { res ->
                    if (!res.isSuccessful || res.body == null) return@withContext null
                    val responseBody = res.body!!.string()
                    JSONObject(responseBody)
                        .optJSONArray("prediction")
                        ?.getJSONArray(0)
                        ?.getDouble(0)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingOverlay.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            finishButton.isEnabled = false
            btnYes.isEnabled = false
            btnNo.isEnabled = false
        } else {
            loadingOverlay.visibility = View.GONE
            progressBar.visibility = View.GONE
            finishButton.isEnabled = true
            btnYes.isEnabled = true
            btnNo.isEnabled = true
        }
    }
}