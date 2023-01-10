package org.listenbrainz.android.presentation.features.brainzplayer.services

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
import org.listenbrainz.android.data.repository.AlbumRepository
import org.listenbrainz.android.data.repository.PlaylistRepository
import org.listenbrainz.android.data.repository.SongRepository
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.LocalMusicSource
import org.listenbrainz.android.presentation.features.brainzplayer.services.callback.BrainzPlayerEventListener
import org.listenbrainz.android.presentation.features.brainzplayer.services.callback.BrainzPlayerNotificationListener
import org.listenbrainz.android.presentation.features.brainzplayer.services.callback.MusicPlaybackPreparer
import org.listenbrainz.android.presentation.features.brainzplayer.services.notification.BrainzPlayerNotificationManager
import org.listenbrainz.android.util.BrainzPlayerExtensions.toMediaMetadataCompat
import org.listenbrainz.android.util.BrainzPlayerUtils.MEDIA_ROOT_ID
import org.listenbrainz.android.util.BrainzPlayerUtils.SERVICE_TAG
import javax.inject.Inject

@AndroidEntryPoint
class BrainzPlayerService: MediaBrowserServiceCompat() {

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
            localMusicSource.setMediaSource(songRepository.getSongsStream().first().map {
                it.toMediaMetadataCompat
            }.toMutableList())
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
                    localMusicSource.songs,
                    currentlyPlayingSong,
                    false
                )
            }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(BrainzPlayerQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
        brainzPlayerEventListener = BrainzPlayerEventListener(this)
        exoPlayer.addListener(brainzPlayerEventListener)
        brainzPlayerNotificationManager.showNotification(exoPlayer)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultSent = localMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(localMusicSource.asMediaItem())
                        if (!isPlayerInitialized && localMusicSource.songs.isNotEmpty()) {
                            preparePlayer(localMusicSource.songs, localMusicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        result.sendResult(null)
                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(brainzPlayerEventListener)
        exoPlayer.release()
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        serviceScope.launch(Dispatchers.Main) {
            val currentSongIndex = if (currentSong == null) 0 else songs.indexOf(itemToPlay)
            exoPlayer.setMediaItems(localMusicSource.asMediaSource())
            exoPlayer.prepare()
            exoPlayer.seekTo(currentSongIndex, 0L)
            exoPlayer.playWhenReady = playNow
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