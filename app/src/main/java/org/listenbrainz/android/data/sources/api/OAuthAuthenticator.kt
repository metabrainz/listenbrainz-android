package org.listenbrainz.android.data.sources.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.listenbrainz.android.data.sources.api.ListenBrainzServiceGenerator.createService
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Log.d
import java.io.IOException

internal class OAuthAuthenticator : Authenticator {
    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        val service = createService(LoginService::class.java, false)
        d("OkHttp : " + response.body.string())
        val refreshToken = LBSharedPreferences.refreshToken
        val call = service.refreshAccessToken(refreshToken,
                "refresh_token",
                ListenBrainzServiceGenerator.CLIENT_ID,
                ListenBrainzServiceGenerator.CLIENT_SECRET)
        val newResponse = call!!.execute()
        val token = newResponse.body()
        if (token != null) {
            val editor = LBSharedPreferences.preferences.edit()
            editor.putString(LBSharedPreferences.REFRESH_TOKEN, token.refreshToken)
            editor.putString(LBSharedPreferences.ACCESS_TOKEN, token.accessToken)
            editor.apply()
        }
        return when {
            token?.accessToken != null -> response.request
                .newBuilder()
                .header("Authorization", "Bearer " + token.accessToken)
                .build()
            else -> null
        }
    }
}