package com.leduytuanvu.vendingmachine.core.di

import com.leduytuanvu.vendingmachine.features.auth.data.repository.AuthRepositoryImpl
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.settings.data.repository.SettingsRepositoryImpl
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
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

    @Binds
    @Singleton
    abstract fun provideAuthRepository(impl: AuthRepositoryImpl) : AuthRepository

    @Binds
    @Singleton
    abstract fun provideSettingRepository(impl: SettingsRepositoryImpl) : SettingsRepository
}