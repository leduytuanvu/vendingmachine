package com.leduytuanvu.vendingmachine.core.di

import android.content.Context
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewModel.SetupSlotViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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