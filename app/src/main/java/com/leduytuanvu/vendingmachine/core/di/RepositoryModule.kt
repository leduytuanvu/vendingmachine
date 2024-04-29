package com.leduytuanvu.vendingmachine.core.di

import com.leduytuanvu.vendingmachine.features.splash.data.repository.SplashRepositoryImpl
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun provideSplashRepository(impl: SplashRepositoryImpl) : SplashRepository
}