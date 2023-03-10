package org.listenbrainz.android.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.android.BuildConfig.APPLICATION_ID
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Song
import java.io.File

object BrainzPlayerExtensions {

    //MediaBrowserCompat extensions
    inline val MediaBrowserCompat.MediaItem.toSong
        get() = Song(
            mediaID = mediaId?.toLong() ?: Song.emptySong.mediaID,
            title = description.title.toString(),
            artist = description.subtitle.toString(),
            albumArt = description.iconUri.toString(),
            uri = description.mediaUri.toString()
        )

    //MediaMetadataCompat extensions
    inline val MediaMetadataCompat?.toSong
        get() = this?.description?.let {
            Song(
                mediaID = (if(it.mediaId!="") it.mediaId?.toLong() else  Song.emptySong.mediaID)!!,
                title = it.title.toString(),
                artist = it.subtitle.toString(),
                uri = it.mediaUri.toString(),
                albumArt = it.iconUri.toString()
            )
        } ?: Song()


    inline val Song.toMediaMetadataCompat: MediaMetadataCompat
        get() = this.let { song ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaID.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.uri)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.albumArt)
                .build()
        }

    private inline val MediaMetadataCompat.mediaUri: Uri
        get() = this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()

    inline val MediaMetadataCompat.id: Long
        get() = getLong(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

    inline val MediaMetadataCompat.title: String?
        get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

    inline val MediaMetadataCompat.artist: String?
        get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

    inline val MediaMetadataCompat.duration
        get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

    inline val MediaMetadataCompat.album: String?
        get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

    private inline val MediaMetadataCompat.albumArtUri: Uri
        get() = this.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI).toUri()


    private fun MediaMetadataCompat.toMediaItemMetadata(): MediaMetadata {
        return with(MediaMetadata.Builder()) {
            setTitle(title)
            setAlbumArtist(artist)
            setAlbumTitle(album)
            setArtworkUri(albumArtUri)
            val extras = Bundle()
            extras.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            setExtras(extras)
        }.build()
    }

    fun MediaMetadataCompat.toMediaItem(): com.google.android.exoplayer2.MediaItem {
        return with(com.google.android.exoplayer2.MediaItem.Builder()) {
            setMediaId(mediaUri.toString())
            setUri(mediaUri)
            setMimeType(MimeTypes.AUDIO_MPEG)
            setMediaMetadata(toMediaItemMetadata())
        }.build()
    }
    //PlaybackStateCompat extensions
    inline val PlaybackStateCompat.isPrepared
        get() = state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED

    inline val PlaybackStateCompat.isPlaying
        get() = state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_PLAYING

    inline val PlaybackStateCompat.isPlayEnabled
        get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
                (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) ||
                ( state == PlaybackStateCompat.STATE_PAUSED)

    inline val PlaybackStateCompat.currentPlaybackPosition: Long
        get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
            val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
            (position + (timeDelta * playbackSpeed)).toLong()
        }
        else position

    //Image Extensions
    suspend fun String.bitmap(context: Context): Bitmap = withContext(Dispatchers.IO) {
        val imageRequest = ImageRequest.Builder(context)
            .data(this@bitmap)
            .size(128)
            .scale(Scale.FILL)
            .allowHardware(false)
            .build()

        when (val result = imageRequest.context.imageLoader.execute(imageRequest)) {
            is SuccessResult -> result.drawable.toBitmap()
            is ErrorResult -> R.drawable.ic_listenbrainz_logo_no_text.bitmap(context)
        }
    }
    suspend fun Int.bitmap(context: Context): Bitmap = withContext(Dispatchers.IO) {
        val imageRequest = ImageRequest.Builder(context)
            .data(this@bitmap)
            .size(128)
            .scale(Scale.FILL)
            .allowHardware(false)
            .build()

        when (val result = imageRequest.context.imageLoader.execute(imageRequest)) {
            is SuccessResult -> result.drawable.toBitmap()
            is ErrorResult -> BitmapFactory.decodeResource(context.resources, R.drawable.ic_listenbrainz_logo_no_text)
        }
    }

    fun File.asAlbumArtContentUri(): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(APPLICATION_ID)
            .appendPath(this.path)
            .build()
    }
}