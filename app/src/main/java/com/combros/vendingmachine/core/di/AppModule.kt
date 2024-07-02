package com.combros.vendingmachine.core.di

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.combros.vendingmachine.BuildConfig
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionHelperDatasource
import com.combros.vendingmachine.core.util.ByteArrays
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.features.auth.data.model.request.LoginRequest
import com.combros.vendingmachine.features.auth.data.remote.AuthApi
import com.combros.vendingmachine.features.home.data.remote.HomeApi
import com.combros.vendingmachine.features.settings.data.remote.SettingsApi
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
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideLocalStorageDatasource(): LocalStorageDatasource {
        return LocalStorageDatasource()
    }

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger
    }

    @Provides
    @Singleton
    fun providePortConnectionDataSource(): PortConnectionDatasource = PortConnectionDatasource()

    @Provides
    @Singleton
    fun providePortConnectionHelperDataSource(): PortConnectionHelperDatasource = PortConnectionHelperDatasource()

    @Provides
    @Singleton
    fun provideByteArrays(): ByteArrays = ByteArrays()

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
                    val dataPassword = Base64.decode(initSetup.password, Base64.DEFAULT)
                    val loginResponse = authApi.login(
                        initSetup.vendCode,
                        LoginRequest(
                            initSetup.username,
                            String(dataPassword, Charsets.UTF_8).substringBefore("567890VENDINGMACHINE", "")
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
                Logger.error("Error in authInterceptor", e)
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
//        .baseUrl(BASE_URL)
        .baseUrl(BuildConfig.API_URL)
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

    private val homeApi: HomeApi = retrofit.create(HomeApi::class.java)
    @Singleton
    @Provides
    fun provideHomeApi(): HomeApi {
        return homeApi
    }
}
