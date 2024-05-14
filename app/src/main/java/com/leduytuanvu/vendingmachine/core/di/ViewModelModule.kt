package com.leduytuanvu.vendingmachine.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

//    @Provides
//    @Singleton
//    fun provideSettingsViewModel(
//        settingsRepository: SettingsRepository,
//        baseRepository: BaseRepository,
//        portConnectionDataSource: PortConnectionDatasource,
//        logger: Logger,
//        context: Context,
//    ): SettingsViewModel {
//        return SettingsViewModel(
//            settingsRepository,
//            baseRepository,
//            portConnectionDataSource,
//            logger,
//            context,
//        )
//    }
}