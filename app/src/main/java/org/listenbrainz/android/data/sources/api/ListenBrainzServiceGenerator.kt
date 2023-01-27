package org.listenbrainz.android.data.sources.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.listenbrainz.android.App
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ListenBrainzServiceGenerator {
    const val API_BASE_URL = "https://musicbrainz.org/ws/2/"
    const val AUTH_BASE_URL = "https://musicbrainz.org/oauth2/"
    const val LISTENBRAINZ_API_BASE_URL = "https://api.listenbrainz.org/"
    const val CLIENT_ID = "XqCukyOoCAH9PRrHpwiINlvJ1T-x4ffQ"
    const val CLIENT_SECRET = "CJrUcF_jyzj-MVCPhNXSwPIpu_eeb_Ye"
    const val OAUTH_REDIRECT_URI = "org.listenbrainz.android://oauth"

    private var authenticator: OAuthAuthenticator? = null
    private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private var headerInterceptor: HeaderInterceptor? = null

    private val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(App.context!!)) {
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build()
                }
                else {
                    request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                }
                chain.proceed(request)
            }

    private val builder = Retrofit.Builder().baseUrl(API_BASE_URL).addConverterFactory(GsonConverterFactory.create())

    private var retrofit = builder.build()

    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
            else -> {
                val nwInfo = connectivityManager.activeNetworkInfo ?: return false
                return nwInfo.isConnected
            }
        }
    }

    fun <S> createService(service: Class<S>, requiresAuthenticator: Boolean): S {
        headerInterceptor = HeaderInterceptor()
        addInterceptors(headerInterceptor)

        // Authenticator should not be added for requests to refresh token and gaining access token
        if (requiresAuthenticator) addAuthenticator()
        addInterceptors(loggingInterceptor)
        return retrofit.create(service)
    }

    private fun addAuthenticator() {
        authenticator = OAuthAuthenticator()
        httpClientBuilder.authenticator(authenticator!!)
        builder.client(httpClientBuilder.build())
        retrofit = builder.build()
    }

    private fun addInterceptors(interceptor: Interceptor?) {
        if (interceptor != null && !httpClientBuilder.interceptors().contains(interceptor)) {
            httpClientBuilder.addInterceptor(interceptor)
            builder.client(httpClientBuilder.build())
            retrofit = builder.build()
        }
    }
}