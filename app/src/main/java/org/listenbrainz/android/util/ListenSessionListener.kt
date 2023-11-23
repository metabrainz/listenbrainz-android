package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.scrobblemanager.ScrobbleManager
import org.listenbrainz.android.util.Log.d
import java.util.concurrent.ConcurrentHashMap

class ListenSessionListener(
    val appPreferences: AppPreferences,
    val scrobbleManager: ScrobbleManager,
    private val serviceScope: CoroutineScope
) : OnActiveSessionsChangedListener {
    
    private val activeSessions: ConcurrentHashMap<MediaController, ListenCallback?> = ConcurrentHashMap()

    @Synchronized
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        d("onActiveSessionsChanged: EXECUTED")
        if (controllers == null) return
        clearSessions()
        registerControllers(controllers)
    }
    
    init {
        serviceScope.launch {
            // Unregistering callback is now reactive.
            appPreferences
                .getListeningBlacklistFlow()
                .distinctUntilChanged()
                .collectLatest { blacklist ->
                    activeSessions.forEach { entry ->
                        if (entry.key.packageName in blacklist) {
                            // Unregister listen callback
                            entry.key.unregisterCallback(entry.value!!)
                            
                            // remove the active session.
                            activeSessions.remove(entry.key)
                            d("### UNREGISTERED MediaController Callback for ${entry.key.packageName}.")
                            return@collectLatest
                        }
                    }
                }
            
        }
    }
    
    private fun registerControllers(controllers: List<MediaController>) {
        val blacklist = runBlocking {
            appPreferences.getListeningBlacklist()
        }
        for (controller in controllers) {
            // BlackList
            if (controller.packageName in blacklist)
                continue

            val callback = ListenCallback(controller.packageName)
            activeSessions[controller] = callback
            controller.registerCallback(callback)
            d("### REGISTERED MediaController callback for ${controller.packageName}.")
        }

        // Adding any new app packages found in the notification.
        serviceScope.launch(Dispatchers.Default) {
            controllers.forEach { controller ->
                val appList = appPreferences.getListeningApps()
                if (controller.packageName !in appList){
                    appPreferences.setListeningApps(appList.plus(controller.packageName))
                }
            }
        }
        
        // println(appPreferences.listeningApps)
    }

    fun clearSessions() {
        for ((controller, callback) in activeSessions) {
            controller.unregisterCallback(callback!!)
            d("### UNREGISTERED MediaController Callback for ${controller.packageName}.")
        }
        activeSessions.clear()
    }

    private inner class ListenCallback(private val player: String) : MediaController.Callback() {
        
        @Synchronized
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            scrobbleManager.onMetadataChanged(metadata, player)
        }
    
        @Synchronized
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            scrobbleManager.onPlaybackStateChanged(state)
        }
        
    }
}
