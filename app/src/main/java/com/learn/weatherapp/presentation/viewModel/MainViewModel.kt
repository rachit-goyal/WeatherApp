package com.learn.weatherapp.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.weatherapp.domain.repository.MainRepository
import com.learn.weatherapp.utils.Resource
import com.learn.weatherapp.data.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
created by Rachit on 3/1/2024.
 */


@HiltViewModel
class MainViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {


    private val _liveData = MutableLiveData<Resource<WeatherData>>()
    val liveData = _liveData

    fun getData(city: String) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getData(city).collectLatest {
                when (it) {
                    is Resource.Error -> {
                        _liveData.postValue(Resource.Error(it.message))
                    }

                    Resource.Loading -> {
                        _liveData.postValue(Resource.Loading)

                    }

                    is Resource.Success -> {
                        it.data?.let { weatherData ->
                            _liveData.postValue(Resource.Success(weatherData))


                        }

                    }
                }
            }
        }
    }
}