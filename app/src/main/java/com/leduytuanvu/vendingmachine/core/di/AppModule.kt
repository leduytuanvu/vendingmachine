package com.leduytuanvu.vendingmachine.core.di

import android.content.Context
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup
//import com.leduytuanvu.vendingmachine.core.room.LogExceptionDao
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.BASE_URL
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.remote.AuthApi
import com.leduytuanvu.vendingmachine.features.settings.data.remote.SettingsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideLocalStorageDatasource(): LocalStorageDatasource {
        return LocalStorageDatasource()
    }

    @Provides
    @Singleton
    fun provideLogger() : Logger {
        return Logger()
    }

    @Provides
    @Singleton
    fun providePortConnectionDataSource(): PortConnectionDatasource = PortConnectionDatasource()

    private var accessToken: String = ""
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        accessToken.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        var response = chain.proceed(request.build())
        if (response.code == 401) {
            try {
                runBlocking {
                    val initSetup: InitSetup = LocalStorageDatasource().getDataFromPath(pathFileInitSetup)!!
                    val loginResponse = authApi.login(
                        initSetup.vendCode,
                        LoginRequest(
                            initSetup.username!!,
                            initSetup.password!!.substringBefore("_leduytuanvu", "")
                        )
                    )
                    accessToken = loginResponse.accessToken!!
                    response.close()
                    val newRequest = chain.request().newBuilder()
                        .header("Authorization", "Bearer ${loginResponse.accessToken}")
                        .build()
                    response = chain.proceed(newRequest)
                }
            } catch (e: Exception) {
                Logger().error("Error in authInterceptor", e)
            }
        }
        response
    }
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logger)
        .addInterceptor(authInterceptor)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    @Singleton
    @Provides
    fun provideAuthApi(): AuthApi {
        return authApi
    }

    private val settingsApi: SettingsApi = retrofit.create(SettingsApi::class.java)
    @Singleton
    @Provides
    fun provideSettingsApi(): SettingsApi {
        return settingsApi
    }
}
