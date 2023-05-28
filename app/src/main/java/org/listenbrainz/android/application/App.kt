package org.listenbrainz.android.application

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.service.ListenScrobbleService
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var listensService: ListensService

    override fun onCreate() {
        super.onCreate()
        context = this

        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP -> {
                startListenService()
            }
        }

        val token = appPreferences.lbAccessToken
        val username = appPreferences.username
        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            listensService.getServicesLinkedToAccount("Bearer: $token", username).enqueue(object : Callback<ListenBrainzExternalServices> {
                override fun onResponse(call: Call<ListenBrainzExternalServices>, response: Response<ListenBrainzExternalServices>) {
                    if (response.isSuccessful) {
                        Log.d("Services found: " + response.body().toString())
                        if (response.body()?.services?.contains("spotify") == true) {
                            Log.d("Spotify is already linked with web.")
                            if(!appPreferences.listeningBlacklist.contains(Constants.SPOTIFY_PACKAGE_NAME)) {
                                Log.d("Adding Spotify to blacklist.")
                                appPreferences.listeningBlacklist = appPreferences.listeningBlacklist.plus(Constants.SPOTIFY_PACKAGE_NAME)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ListenBrainzExternalServices?>, t: Throwable) {
                    Log.d("Services not found")
                }
            })
        }
    }

    fun startListenService() {
        val intent = Intent(this.applicationContext, ListenScrobbleService::class.java)
        if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED) {
            startService(intent)
        }
    }

    fun stopListenService() {
        val intent = Intent(this.applicationContext, ListenScrobbleService::class.java)
        stopService(intent)
    }

    companion object {
        var context: App? = null
            private set
    }
}