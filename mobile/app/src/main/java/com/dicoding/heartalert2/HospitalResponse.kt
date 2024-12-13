package com.dicoding.heartalert2

data class HospitalResponse(
    val hospitals: List<Hospital>
)

data class Hospital(
    val name: String,
    val location: Location,
    val duration: String
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)
