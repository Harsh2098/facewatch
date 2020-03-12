package com.hmproductions.facewatch.dagger

import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.utils.Constants.FACENET_API_BASE_URL
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class ClientModule {

    @Provides
    @FaceWatchApplicationScope
    fun getFaceWatchRetrofit(interceptor: HttpLoggingInterceptor): Retrofit =
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(interceptor)).baseUrl(FACENET_API_BASE_URL).build()

    @Provides
    @FaceWatchApplicationScope
    fun getFaceWatchRetrofitClient(retrofit: Retrofit): FaceWatchClient = retrofit.create(
        FaceWatchClient::class.java
    )

    private fun getOkHttpClient(interceptor: HttpLoggingInterceptor) =
        OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).addInterceptor(interceptor).build()

    @Provides
    @FaceWatchApplicationScope
    fun getInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}
