package org.listenbrainz.sharedtest.utils

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.listenbrainz.android.util.HeaderInterceptor
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils {
    fun <S> createTestService(service: Class<S>, baseUrl: HttpUrl): S {
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient()
                    .newBuilder()
                    .addInterceptor(HeaderInterceptor(MockAppPreferences()))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
        return retrofit.create(service)
    }
}