package org.listenbrainz.android.util

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants.Headers.AUTHORIZATION
import java.io.IOException
import javax.inject.Inject


class HeaderInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
): Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        request = request.newBuilder()
            .addHeader(AUTHORIZATION, "Token ${appPreferences.lbAccessToken}")
            .build()
        
        return chain.proceed(request)
    }
}