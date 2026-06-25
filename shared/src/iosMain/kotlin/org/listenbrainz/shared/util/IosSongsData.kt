package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.Song
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.MediaPlayer.MPMediaItem
import platform.MediaPlayer.MPMediaItemPropertyAlbumPersistentID
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyAlbumTrackNumber
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtistPersistentID
import platform.MediaPlayer.MPMediaItemPropertyDateAdded
import platform.MediaPlayer.MPMediaItemPropertyDiscNumber
import platform.MediaPlayer.MPMediaItemPropertyPersistentID
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyReleaseDate
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPMediaLibrary
import platform.MediaPlayer.MPMediaLibraryAuthorizationStatus
import platform.MediaPlayer.MPMediaLibraryAuthorizationStatusAuthorized
import platform.MediaPlayer.MPMediaQuery

class IosSongsData(): SongsData() {
    override fun songs(): List<Song> {
        if(MPMediaLibrary.authorizationStatus()!= MPMediaLibraryAuthorizationStatusAuthorized){
            return emptyList()
        }

        val songs = mutableListOf<Song>()
        val mediaItems = MPMediaQuery.songsQuery().items ?: return emptyList()

        for(item in mediaItems){
            val mediaItem = item as? MPMediaItem
            if(mediaItem == null){
                continue
            }
            val id = mediaItem.valueForProperty(MPMediaItemPropertyPersistentID) as? Long ?: 0L
            val albumID = mediaItem.valueForProperty(MPMediaItemPropertyAlbumPersistentID) as? Long ?: 0L
            val year = (mediaItem.valueForProperty(MPMediaItemPropertyReleaseDate) as? NSDate)?.let {
                NSCalendar.currentCalendar.component(NSCalendarUnitYear,it).toInt()
            }
            val durationLong = (mediaItem.valueForProperty(MPMediaItemPropertyPlaybackDuration) as? Double)?.let {
                (it*1000).toLong()
            }
            val dateModified = (mediaItem.valueForProperty(MPMediaItemPropertyDateAdded) as? NSDate)?.let {
                (it.timeIntervalSince1970 * 1000).toLong()
            }
            val contentUri = "mediaitem://$id"
            val albumArt = "mediaitem-art://$albumID"
            songs += Song(
                mediaID = id,
                title = mediaItem.valueForProperty(MPMediaItemPropertyTitle) as? String ?: "Title",
                artist = mediaItem.valueForProperty(MPMediaItemPropertyArtist) as? String  ?: "Artist",
                albumID = albumID,
                album =  mediaItem.valueForProperty(MPMediaItemPropertyAlbumTitle) as? String ?: "Album",
                year = year ?: 0,
                duration = durationLong ?: 0L,
                trackNumber = (mediaItem.valueForProperty(MPMediaItemPropertyAlbumTrackNumber) as? Long)?.toInt() ?: 0,
                discNumber = mediaItem.valueForProperty(MPMediaItemPropertyDiscNumber) as? Long ?: 1L,
                artistId = mediaItem.valueForProperty(MPMediaItemPropertyArtistPersistentID) as? Long ?: 0L,
                dateModified = dateModified ?: 0L,
                uri = contentUri,
                albumArt = albumArt
            )
        }
        return songs
    }
}