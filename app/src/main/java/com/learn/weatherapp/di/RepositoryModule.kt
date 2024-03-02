package com.learn.weatherapp.di

import com.learn.weatherapp.data.repository.MainRepoImpl
import com.learn.weatherapp.domain.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
created by Rachit on 3/1/2024.
 */

@InstallIn(ViewModelComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindsRepoImpl(repoImpl: MainRepoImpl): MainRepository
}