package org.listenbrainz.android.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.repository.brainzplayer.AlbumRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.android.util.BrainzPlayerExtensions.toMediaMetadataCompat
import org.listenbrainz.android.util.BrainzPlayerNotificationManager
import org.listenbrainz.android.util.BrainzPlayerUtils.MEDIA_ROOT_ID
import org.listenbrainz.android.util.BrainzPlayerUtils.SERVICE_TAG
import org.listenbrainz.android.util.LocalMusicSource
import javax.inject.Inject

@AndroidEntryPoint
class BrainzPlayerService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var localMusicSource: LocalMusicSource

    @Inject
    lateinit var albumRepository: AlbumRepository

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var appPreferences: AppPreferences

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector
    private lateinit var brainzPlayerEventListener : BrainzPlayerEventListener
    private lateinit var brainzPlayerNotificationManager : BrainzPlayerNotificationManager

    var isForegroundService = false
    private var isPlayerInitialized = false
    private var currentSong: MediaMetadataCompat? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        var currentSongDuration = 0L
            private set
    }
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            if (appPreferences.currentPlayable == null){
                appPreferences.currentPlayable = Playable(PlayableType.ALL_SONGS, -1L, songRepository.getSongsStream().first().map {
                    it
                }, 0 )
            }
            localMusicSource.setMediaSource(appPreferences.currentPlayable?.songs?.map { song->
                song.toMediaMetadataCompat }?.toMutableList() ?: mutableListOf() )
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        brainzPlayerNotificationManager = BrainzPlayerNotificationManager(this, mediaSession.sessionToken, BrainzPlayerNotificationListener(this)) {
            currentSongDuration = exoPlayer.duration
        }
        val musicPlaybackPreparer =
            MusicPlaybackPreparer(localMusicSource) { currentlyPlayingSong ->
                currentSong = currentlyPlayingSong
                preparePlayer(
                    true
                )
            }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(BrainzPlayerQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
        brainzPlayerEventListener = BrainzPlayerEventListener(this)
        exoPlayer.addListener(brainzPlayerEventListener)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    var isResultSent = false
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        if (parentId == MEDIA_ROOT_ID) {
            localMusicSource.whenReady { isInitialized ->
                if (isInitialized) {
                    if (!isResultSent) {
                        result.sendResult(localMusicSource.asMediaItem())
                        isResultSent = true
                    }
                    if (!isPlayerInitialized && localMusicSource.songs.isNotEmpty()) {
                        preparePlayer(playNow = false)
                        isPlayerInitialized = true
                    }
                } else {
                    result.sendResult(mutableListOf())
                }
            }
        }else{
            result.sendResult(null)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
        brainzPlayerNotificationManager.hideNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(brainzPlayerEventListener)
        exoPlayer.release()
    }

    private fun preparePlayer(
        playNow: Boolean
    ) {
        serviceScope.launch(Dispatchers.Main) {
            val songs = appPreferences.currentPlayable?.songs?.map {
                it.toMediaMetadataCompat
            }?.toMutableList() ?: mutableListOf()
            localMusicSource.setMediaSource(songs)
            val currentSongIndex = appPreferences.currentPlayable?.currentSongIndex ?: 0
            exoPlayer.setMediaItems(localMusicSource.asMediaSource())
            exoPlayer.prepare()
            exoPlayer.seekTo(currentSongIndex,appPreferences.currentPlayable?.seekTo ?: 0L)
            exoPlayer.playWhenReady = playNow
            brainzPlayerNotificationManager.showNotification(exoPlayer)
        }
    }

    private inner class BrainzPlayerQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex< localMusicSource.songs.size){
                return localMusicSource.songs[windowIndex].description
            }
            return MediaDescriptionCompat.Builder().build()
        }
    }
}
