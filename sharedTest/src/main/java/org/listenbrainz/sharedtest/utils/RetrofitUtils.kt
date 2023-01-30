package org.listenbrainz.sharedtest.utils

import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils {
    fun <S> createTestService(service: Class<S>, baseUrl: HttpUrl?): S {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl!!)
            .build()
        return retrofit.create(service)
    }
}