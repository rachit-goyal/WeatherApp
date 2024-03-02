package com.learn.weatherapp.data.remote

import com.learn.weatherapp.data.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

/**
created by Rachit on 3/1/2024.
 */
interface ApiService {

    @GET("data/2.5/weather")
    suspend fun getData(
        @Query("q") city: String,
        @Query("appid") appid: String = Companion.appid,
        @Query("units") units: String = "metric"
    ): WeatherData?

    companion object {
        const val appid = "19259a9dcbdd2440b177714b57e53dde"
    }
}