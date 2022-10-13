package org.listenbrainz.android.data.sources.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.listenbrainz.android.presentation.features.login.LoginSharedPreferences
import java.io.IOException

internal class HeaderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        // Do not add any headers if fetching data from wiki
        if (original.url.host.contains("wiki")) return chain.proceed(original)

        // Do not add Authorization Header if request is sent to OAuth endpoint except to fetch userinfo
        val request: Request = if (original.url.encodedPath.contains("oauth2")) {
            if (original.url.encodedPath.contains("userinfo")) original.newBuilder()
                    .header("User-agent", "MusicBrainzAndroid/Test (kartikohri13@gmail.com)")
                    .addHeader("Accept", "application/json")
                    .header("Authorization", " Bearer $accessToken").build() else return chain.proceed(original)
        } else {
            if (checkLoginStatus()) {
                original.newBuilder()
                        .header("User-agent", "MusicBrainzAndroid/Test (kartikohri13@gmail.com)")
                        .addHeader("Accept", "application/json")
                        .header("Authorization", " Bearer $accessToken").build()
            } else {
                original.newBuilder()
                        .header("User-agent", "MusicBrainzAndroid/Test (kartikohri13@gmail.com)")
                        .addHeader("Accept", "application/json").build()
            }
        }
        return chain.proceed(request)
    }

    private fun checkLoginStatus(): Boolean {
        return (LoginSharedPreferences.loginStatus == LoginSharedPreferences.STATUS_LOGGED_IN)
    }

    private val accessToken: String get() = LoginSharedPreferences.accessToken!!
}