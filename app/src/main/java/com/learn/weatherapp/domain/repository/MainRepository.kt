package com.learn.weatherapp.domain.repository

import com.learn.weatherapp.utils.Resource
import com.learn.weatherapp.data.model.WeatherData
import kotlinx.coroutines.flow.Flow

/**
created by Rachit on 3/1/2024.
 */
interface MainRepository {

    suspend fun getData(city:String): Flow<Resource<WeatherData?>>
}