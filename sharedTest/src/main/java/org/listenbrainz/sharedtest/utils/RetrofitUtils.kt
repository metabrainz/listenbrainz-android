package org.listenbrainz.sharedtest.utils

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.listenbrainz.android.util.HeaderInterceptor
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import retrofit2.Retrofit

object RetrofitUtils {
    val json by lazy { kotlinx.serialization.json.Json {  } }
    fun <S> createTestService(service: Class<S>, baseUrl: HttpUrl): S {
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .addInterceptor(HeaderInterceptor(MockAppPreferences()))
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .build()
        return retrofit.create(service)
    }
}