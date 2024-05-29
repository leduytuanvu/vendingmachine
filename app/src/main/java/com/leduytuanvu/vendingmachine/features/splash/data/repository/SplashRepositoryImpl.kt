package com.leduytuanvu.vendingmachine.features.splash.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.leduytuanvu.vendingmachine.ScheduledTaskWorker
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor() : SplashRepository