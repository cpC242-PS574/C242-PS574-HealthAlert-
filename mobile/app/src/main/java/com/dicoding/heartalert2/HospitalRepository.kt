package com.dicoding.heartalert2

import com.dicoding.heartalert2.api.ApiService

class HospitalRepository(private val apiService: ApiService) {

    suspend fun getHospitals(latitude: Double, longitude: Double): List<Hospital> {
        // Pastikan ini menggunakan LocationRequest yang benar
        val locationRequest = LocationRequest(latitude, longitude)
        val response = apiService.getHospitals(locationRequest)
        return response.hospitals
    }
}