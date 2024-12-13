package com.dicoding.heartalert2.api

import com.dicoding.heartalert2.HospitalResponse
import com.dicoding.heartalert2.LocationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("articles")
    suspend fun getArticles(): Response<ArticleResponse>

    @GET("articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Response<ArticlesItem>

    @POST("hospital")
    suspend fun getHospitals( @Body locationRequest: LocationRequest): HospitalResponse
}