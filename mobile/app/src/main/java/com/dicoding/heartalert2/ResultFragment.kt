package com.dicoding.heartalert2

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.adapter.ArticleAdapter
import com.dicoding.heartalert2.adapter.HospitalAdapter
import com.dicoding.heartalert2.api.ArticlesItem
import com.dicoding.heartalert2.api.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultFragment : Fragment(R.layout.fragment_result) {

    private lateinit var appDataStore: AppDataStore
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var recyclerViewArticles: RecyclerView
    private lateinit var recyclerViewHospitals: RecyclerView
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLocationAndFetchHospitals() // Meminta lokasi dan data rumah sakit
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val activityBpmTextView: TextView = view.findViewById(R.id.activityBpmTextView)
        val riskStatusTextView: TextView = view.findViewById(R.id.riskStatusTextView)
        val btnRemeasure: Button = view.findViewById(R.id.btn_remeasure)
        recyclerViewArticles = view.findViewById(R.id.recyclerViewHealthArticles)
        recyclerViewHospitals = view.findViewById(R.id.recyclerViewHospital)

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Menampilkan hasil ketika fragment dibuat
        loadResults(activityBpmTextView, riskStatusTextView)

        // Mengatur RecyclerView untuk artikel dan rumah sakit
        recyclerViewArticles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHospitals.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Menambahkan listener untuk tombol "Mengukur Ulang"
        btnRemeasure.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_introFragment)
        }

        // Memuat artikel
        loadArticles()

        // Meminta izin lokasi
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Lokasi sudah diizinkan, langsung ambil lokasi dan data rumah sakit
            getLocationAndFetchHospitals()
        } else {
            // Lokasi belum diizinkan, minta izin dari pengguna
            requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun loadResults(activityBpmTextView: TextView, riskStatusTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Mengambil dan menampilkan activityBpm
                launch {
                    appDataStore.userInputFlow.collect { userInput ->
                        val activityBpm = "${userInput.activityBpm} BPM"
                        activityBpmTextView.text = activityBpm
                        saveToSharedPreferences(userInput.activityBpm.toString(), riskStatusTextView.text.toString())
                    }
                }

                // Mengambil dan menampilkan hasil prediksi dan status risiko
                launch {
                    appDataStore.predictionResultFlow.collect { prediction ->
                        val displayPrediction = prediction ?: 0.0
                        val riskStatus = if (displayPrediction >= 0.5) "Beresiko!" else "Normal"
                        riskStatusTextView.text = riskStatus
                        saveToSharedPreferences(activityBpmTextView.text.toString(), riskStatus)
                    }
                }
            }
        }
    }

    private fun saveToSharedPreferences(activityBpm: String, riskStatus: String) {
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val result = "$activityBpm, $riskStatus"
        sharedPreferencesHelper.saveMeasurementResult(date, result)
    }

    private fun loadArticles() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getArticles()
                if (response.isSuccessful) {
                    val articleList = response.body()?.articles ?: emptyList()
                    setupArticleRecyclerView(articleList)
                } else {
                    Toast.makeText(context, "Failed to load articles.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupArticleRecyclerView(articleList: List<ArticlesItem>) {
        articleAdapter = ArticleAdapter(articleList)
        recyclerViewArticles.adapter = articleAdapter
    }

    private fun getLocationAndFetchHospitals() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Izin lokasi sudah diberikan, dapatkan lokasi pengguna
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude

                        // Memanggil API untuk mengambil data rumah sakit berdasarkan koordinat
                        fetchHospitals(latitude, longitude)
                    } ?: run {
                        Toast.makeText(context, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Gagal mendapatkan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Izin lokasi tidak diberikan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Izin belum diberikan, minta izin dari pengguna
            requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchHospitals(latitude: Double, longitude: Double) {
        val locationRequest = LocationRequest(latitude, longitude)

        Log.d("DEBUG", "LocationRequest: Latitude = $latitude, Longitude = $longitude")

        lifecycleScope.launch {
            try {
                // Memanggil API untuk mengambil data rumah sakit berdasarkan koordinat
                val response = RetrofitInstance.api.getHospitals(LocationRequest(latitude, longitude))
                if (response.hospitals.isNotEmpty()) {
                    setupHospitalRecyclerView(response.hospitals)
                } else {
                    Toast.makeText(context, "No hospitals found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHospitalRecyclerView(hospitals: List<Hospital>) {
        val hospitalAdapter = HospitalAdapter(hospitals)
        recyclerViewHospitals.adapter = hospitalAdapter
    }
}