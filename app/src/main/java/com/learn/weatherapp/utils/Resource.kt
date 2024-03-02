package com.learn.weatherapp.utils

/**
created by Rachit on 12/25/2023.
 */
sealed class Resource<out T> {
    data object Loading: Resource<Nothing>()
    data class Error(val message: String): Resource<Nothing>()
    data class Success<T>(val data:T): Resource<T>()
}