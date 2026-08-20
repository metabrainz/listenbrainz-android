package org.listenbrainz.shared.util

import android.media.MediaMetadata
import org.listenbrainz.shared.model.PlayingTrack

fun MediaMetadata.extractTitle(): String? = when {
    !getString(MediaMetadata.METADATA_KEY_TITLE)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_TITLE
    )

    !getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_DISPLAY_TITLE
    )

    else -> null
}

fun MediaMetadata.extractArtist(): String? = when {
    !getString(MediaMetadata.METADATA_KEY_ARTIST)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_ARTIST
    )

    !getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_ALBUM_ARTIST
    )

    !getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE
    )

    !getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
        .isNullOrEmpty() -> getString(
        MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION
    )

    else -> null
}

fun MediaMetadata.extractDuration(): Long = getLong(MediaMetadata.METADATA_KEY_DURATION)

fun MediaMetadata.extractReleaseName(): String? =
    getString(MediaMetadata.METADATA_KEY_ALBUM)
        .takeIf { !it.isNullOrEmpty() }
        ?: getString(MediaMetadata.METADATA_KEY_COMPILATION)


fun MediaMetadata.toPlayingTrack(pkgName: String): PlayingTrack {
    return PlayingTrack(
        timestamp = System.currentTimeMillis(),
        artist = extractArtist(),
        title = extractTitle(),
        duration = extractDuration(),
        releaseName = extractReleaseName(),
        pkgName = pkgName
    )
}