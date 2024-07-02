package com.combros.vendingmachine.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

//    @Provides
//    @Singleton
//    fun provideSetupSlotViewModel(
//        settingsRepository: SettingsRepository,
//        baseRepository: BaseRepository,
//        logger: Logger,
//        context: Context,
//    ): SetupSlotViewModel {
//        return SetupSlotViewModel(
//            settingsRepository,
//            baseRepository,
//            logger,
//            context,
//        )
//    }
}