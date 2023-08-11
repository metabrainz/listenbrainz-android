package org.listenbrainz.android.util

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.media.session.PlaybackState
import androidx.work.WorkManager
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Log.d

class ListenSessionListener(val appPreferences: AppPreferences, val workManager: WorkManager) : OnActiveSessionsChangedListener {
    
    private val activeSessions: MutableMap<MediaController, ListenCallback?> = HashMap()

    @Synchronized
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        d("onActiveSessionsChanged: EXECUTED")
        if (controllers == null) return
        clearSessions()
        registerControllers(controllers)
    }

    private fun registerControllers(controllers: List<MediaController>) {
        for (controller in controllers) {
            // BlackList
            if (controller.packageName in appPreferences.listeningBlacklist)
                continue

            val callback = ListenCallback(controller.packageName)
            activeSessions[controller] = callback
            controller.registerCallback(callback)
            d("### REGISTERED MediaController callback for ${controller.packageName}.")
        }

        // Adding any new app packages found in the notification.
        controllers.forEach { controller ->
            val appList = appPreferences.listeningApps
            if (controller.packageName !in appList){
                appPreferences.listeningApps = appList.plus(controller.packageName)
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
        val listenSubmissionState: ListenSubmissionState = ListenSubmissionState()
        
        // FIXME:
        //  1) First ever session song isn't recorded. This is because onMetadataChanged
        //      isn't called by callback itself.
        
        override fun onMetadataChanged(metadata: MediaMetadata?) {
    
            listenSubmissionState.initSubmissionFlow(metadata){
                // Submit a listen.
                workManager.enqueue(listenSubmissionState.buildWorkRequest(it, player))
            }
            
        }
    
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            listenSubmissionState.toggleTimer(state)
        }
        
    }
}
