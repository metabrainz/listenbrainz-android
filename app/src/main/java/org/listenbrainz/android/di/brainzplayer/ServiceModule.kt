package org.listenbrainz.android.di.brainzplayer

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import org.listenbrainz.android.repository.brainzplayer.AlbumRepository
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.android.util.LocalMusicSource
import org.listenbrainz.android.util.MusicSource

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun providesExoPlayer(
        @ApplicationContext context : Context,
        audioAttributes: AudioAttributes
    ) = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes,true)
        setHandleAudioBecomingNoisy(true)
    }

    @ServiceScoped
    @Provides
    fun providesMusicSource(songRepository: SongRepository,
                            albumRepository: AlbumRepository,
                            artistRepository: AlbumRepository,
                            playlistRepository: PlaylistRepository
    ): MusicSource<MediaMetadataCompat> =
        LocalMusicSource(songRepository, albumRepository, artistRepository,playlistRepository)
}