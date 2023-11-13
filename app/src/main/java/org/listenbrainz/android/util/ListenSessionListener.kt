package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Log.d

class ListenSessionListener(val appPreferences: AppPreferences, val workManager: WorkManager, private val serviceScope: CoroutineScope) : OnActiveSessionsChangedListener {
    
    private val activeSessions: MutableMap<MediaController, ListenCallback?> = HashMap()
    private var controllers: List<MediaController>? = null

    @Synchronized
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        this.controllers = controllers
        d("onActiveSessionsChanged: EXECUTED")
        if (controllers == null) return
        clearSessions()
        registerControllers(controllers)
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

    fun isMediaPlaying() =
        controllers?.any {
            it.playbackState?.state == PlaybackState.STATE_PLAYING &&
                    !it.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).isNullOrEmpty() &&
                    !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrEmpty()
        } ?: false

    private inner class ListenCallback(private val player: String) : MediaController.Callback() {
        val listenSubmissionState: ListenSubmissionState = ListenSubmissionState()
        
        // FIXME:
        //  1) First ever session song isn't recorded. This is because onMetadataChanged
        //      isn't called by callback itself.
        
        override fun onMetadataChanged(metadata: MediaMetadata?) {
    
            listenSubmissionState.initSubmissionFlow(metadata){
                // Submit a listen.
                when {
                    appPreferences.submitListens -> {
                        workManager.enqueue(listenSubmissionState.buildWorkRequest(it, player))
                    }
                }
            }
            
        }
    
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            listenSubmissionState.toggleTimer(state)
        }
        
    }
}
