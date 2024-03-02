package com.learn.weatherapp.data.repository

import com.learn.weatherapp.domain.repository.MainRepository
import com.learn.weatherapp.utils.Resource
import com.learn.weatherapp.data.model.WeatherData
import com.learn.weatherapp.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
created by Rachit on 3/1/2024.
 */
class MainRepoImpl @Inject constructor(val apiService: ApiService): MainRepository {

    override suspend fun getData(city: String): Flow<Resource<WeatherData?>> = flow {
        emit(Resource.Loading)
        try {
            val data = apiService.getData(city)
            emit(Resource.Success(data))
        } catch (e: Exception) {
            emit(Resource.Error("City not Found"))

        }
    }.flowOn(Dispatchers.IO)

}