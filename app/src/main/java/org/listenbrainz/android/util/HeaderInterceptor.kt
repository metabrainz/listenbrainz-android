package org.listenbrainz.android.util

import com.google.common.net.HttpHeaders.AUTHORIZATION
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.listenbrainz.shared.repository.AppPreferences
import java.io.IOException

class HeaderInterceptor (
    private val appPreferences: AppPreferences
): Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        
        return runBlocking {
            if (request.headers[AUTHORIZATION].isNullOrEmpty()) {
                runCatching {
                    withTimeout(3000) {
                        val accessToken = appPreferences.lbAccessToken.get()
                        if (accessToken.isNotEmpty()) {
                            request = request.newBuilder()
                                .addHeader(AUTHORIZATION, "Token $accessToken")
                                .build()
                        }
                    }
                }.getOrElse { it.printStackTrace() }
            }
            chain.proceed(request)
        }
    }
}