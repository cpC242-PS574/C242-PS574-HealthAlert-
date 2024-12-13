package com.dicoding.intro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.SharedPreferencesHelper
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.UserInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RestingBpmFragment : Fragment(R.layout.fragment_resting_bpm) {
    private lateinit var startStopButton: Button
    private lateinit var bpmTextView: TextView
    private lateinit var previewView: PreviewView
    private lateinit var timerTextView: TextView
    private lateinit var nextButton: Button
    private lateinit var appDataStore: AppDataStore

    private var isMonitoring = false
    private var sampleBuffer = mutableListOf<Pair<Long, Double>>()
    private val maxSamples = 60 * 5
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var timer: CountDownTimer? = null
    private var lastToastTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        startStopButton = view.findViewById(R.id.start_stop_button)
        bpmTextView = view.findViewById(R.id.bpm_text)
        previewView = view.findViewById(R.id.preview_view)
        timerTextView = view.findViewById(R.id.timer_text)
        nextButton = view.findViewById(R.id.btn_next)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startStopButton.setOnClickListener { toggleMonitoring() }

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            stopMonitoring(saveResult = true) // Hentikan pengukuran
            findNavController().popBackStack()
//            val chestPainQuestionAnswer = sharedPreferencesHelper.getInt("chestPainLevel", -1)
//            if (chestPainQuestionAnswer == 0) {
//            // Jika pengguna memilih 'no' pada chest pain question
//                (activity as MainActivity).moveToPage(3)
//            } else {
//                (activity as MainActivity).moveToPage(4)
//            }
        }

        // Disable next button initially
        nextButton.isEnabled = false
        nextButton.setOnClickListener {
            // Ensure BPM measurement is completed before proceeding
            if (!isMonitoring) {
                findNavController().navigate(R.id.action_restingBpmFragment_to_activityBpmFragment)
            } else {
//                Toast.makeText(requireContext(), "Selesaikan pengukuran terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun toggleMonitoring() {
        if (isMonitoring) {
            stopMonitoring(saveResult = true)
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        if (!permissionsGranted()) {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            return
        }

        isMonitoring = true
        sampleBuffer.clear()
        bpmTextView.text = "Mengukur..."
        startStopButton.text = "Berhenti"

        startTimer()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder().build(). also {
                it.setAnalyzer(cameraExecutor) { image ->
                    val brightness = averageBrightness(image)
                    val timestamp = System.currentTimeMillis()

                    requireActivity().runOnUiThread {
                        if (isMonitoring) {
                            sampleBuffer.add(timestamp to brightness)
                            if (sampleBuffer.size > maxSamples) {
                                sampleBuffer.removeAt(0)
                            }

                            val bpm = calculateBpm(analyzeData(sampleBuffer).crossings)
                            if (bpm != null && (bpm < 40 || bpm > 200)) {
                                resetData()
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastToastTime > 5000) { // 5 seconds delay between toasts
                                    Toast.makeText(requireContext(),"Pastikan jari anda menutupi kamera.",Toast.LENGTH_SHORT).show()
                                    lastToastTime = currentTime
                                }
                            }
                        }
                    }

                    image.close()
                }
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                camera?.cameraControl?.enableTorch(true)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        cameraProviderFuture.addListener({
            requireActivity().runOnUiThread {
                previewView.postDelayed({
                    startSampling()
                }, 1500)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        startStopButton.postDelayed(updateBpmRunnable, 1500)
    }

    private fun stopMonitoring(saveResult: Boolean) {
        Log.d("RestingBpmFragment", "stopMonitoring: Monitoring dihentikan. saveResult=$saveResult")

        isMonitoring = false
        camera?.cameraControl?.enableTorch(false)
        camera?.let {
            val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
            cameraProvider.unbindAll()
        }
        camera = null
        startStopButton.text = "Mulai"

        timer?.cancel()

        if (saveResult) {
            val dataStats = analyzeData(sampleBuffer)
            val finalBpm = calculateBpm(dataStats.crossings)
            Log.d("RestingBpmFragment", "stopMonitoring: BPM final=$finalBpm, DataStats=$dataStats.")

            finalBpm?.let {
                val roundedBpm = it.roundToInt()
                if (roundedBpm in 40..200) { // Rentang valid BPM
                    bpmTextView.text = "Hasil: $roundedBpm BPM"
                    saveBpm(roundedBpm)
//                    Toast.makeText(requireContext(), "Resting BPM berhasil disimpan: $roundedBpm", Toast.LENGTH_SHORT).show()
                } else {
                    bpmTextView.text = "Pengukuran tidak valid. Coba lagi."
//                    Toast.makeText(requireContext(), "Nilai BPM tidak valid. Pastikan jari Anda stabil.", Toast.LENGTH_SHORT).show()
                }
            } ?: run{
                bpmTextView.text = "Gagal menghitung BPM. Coba lagi."
                // Handdle case where BPM calculation fails
                Log.d("RestingBpmFragment", "BPM calculation failed")
            }
            nextButton.isEnabled = true
        }

        startStopButton.removeCallbacks(updateBpmRunnable)
    }

    private fun resetData() {
        sampleBuffer.clear()
        bpmTextView.text = "Mengukur..."
        restartTimer()
    }

    private fun startTimer() {
        timer?.cancel() // Ensure no existing timer is running
        timer = object : CountDownTimer(13000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "Timer: $secondsRemaining detik"
            }

            override fun onFinish() {
                stopMonitoring(saveResult = true)
            }
        }
        timer?.start()
    }

    private fun restartTimer() {
        timer?.cancel()
        startTimer()
    }

    private fun startSampling() {
//        Toast.makeText(requireContext(), "Memulai Pengukuran...", Toast.LENGTH_SHORT).show()
    }

    private val updateBpmRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                val dataStats = analyzeData(sampleBuffer)
                val bpm = calculateBpm(dataStats.crossings)
                Log.d("RestingBpmFragment", "updateBpmRunnable: DataStats=$dataStats, BPM=$bpm.")

                bpm?.let {
                    bpmTextView.text = "${it.roundToInt()} BPM"
                } ?: run{
                    Log.e("RestingBpmFragment", "updateBpmRunnable: Gagal menghitung BPM.")
                }
                startStopButton.postDelayed(this, 1000)
            }
        }
    }

    private fun calculateBpm(crossings: List<Long>): Double? {
        if (crossings.size < 2) return null

        val averageInterval = (crossings.last() - crossings.first()).toDouble() / (crossings.size - 1)
        return 60000 / averageInterval
    }

    private fun analyzeData(samples: List<Pair<Long, Double>>): DataStats {
        if (samples.isEmpty()) {
            Log.e("RestingBpmFragment", "Samples kosong pada analyzeData.")
            return DataStats(0.0, 0.0, 0.0, 0.0, emptyList())
        }

        val average = samples.map { it.second }.average()
        val min = samples.minOf { it.second }
        val max = samples.maxOf { it.second }
        val range = max - min

        Log.d("RestingBpmFragment", "analyzeData: Jumlah samples=${samples.size}, average=$average, min=$min, max=$max, range=$range.")

        val crossings = getAverageCrossings(samples, average)
        Log.d("RestingBpmFragment", "analyzeData: Jumlah crossings=${crossings.size}.")

        return DataStats(average, min, max, range, crossings)
    }

    private fun getAverageCrossings(samples: List<Pair<Long, Double>>, average: Double): List<Long> {
        if (samples.isEmpty()) {
            Log.e("RestingBpmFragment", "Samples kosong pada getAverageCrossings.")
            return emptyList()
        }

        val crossingsSamples = mutableListOf<Long>()
        var previousSample = samples[0]

        for (currentSample in samples) {
            if (currentSample.second < average && previousSample.second > average) {
                crossingsSamples.add(currentSample.first)
                Log.d("RestingBpmFragment", "getAverageCrossings: Crossing ditemukan pada timestamp=${currentSample.first}.")
            }
            previousSample = currentSample
        }

        Log.d("RestingBpmFragment", "getAverageCrossings: Total crossings=${crossingsSamples.size}.")
        return crossingsSamples
    }

    private fun averageBrightness(image: ImageProxy): Double {
        val buffer: ByteBuffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        var sum = 0
        for (i in data.indices step 4) {
            sum += data[i].toInt() and 0xFF
            sum += data[i + 1].toInt() and 0xFF
        }

        val avg = sum / (data.size * 0.5)
        val brightness = avg / 255
        Log.d("RestingBpmFragment", "averageBrightness: Brightness rata-rata=$brightness.")
        return brightness
    }

    private fun permissionsGranted() = arrayOf(
        Manifest.permission.CAMERA
    ).all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!permissionsGranted()) {
                Toast.makeText(requireContext(), "Izin kamera diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring(saveResult = true)
        cameraExecutor.shutdown()
    }

    private fun saveBpm(bpm: Int) {
        if (bpm > 0) {
            Log.d("RestingBpmFragment", "Saving BPM: $bpm")

            lifecycleScope.launch {
                val currentInput = appDataStore.userInputFlow.first()
                appDataStore.saveUserInput(
                        gender = currentInput.gender,
                        age = currentInput.age,
                        chestPainLevel = currentInput.chestPainLevel,
                        restingBpm = bpm,
                        activityBpm = currentInput.activityBpm,
                        chestTightness = currentInput.chestTightness,
                        date = currentInput.date
                )
            }


        } else {
            Log.d("RestingBpmFragment", "Invalid BPM value, not saving")
//            Toast.makeText(requireContext(), "Nilai BPM tidak Valid", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val TAG = "RestingBpmFragment"
    }

    data class DataStats(
        val average: Double,
        val min: Double,
        val max: Double,
        val range: Double,
        val crossings: List<Long>
    )
}