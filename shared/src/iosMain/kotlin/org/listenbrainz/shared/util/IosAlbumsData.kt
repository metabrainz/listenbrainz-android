package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.Album
import platform.Foundation.NSNumber
import platform.MediaPlayer.MPMediaItem
import platform.MediaPlayer.MPMediaItemCollection
import platform.MediaPlayer.MPMediaItemPropertyAlbumPersistentID
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaLibrary
import platform.MediaPlayer.MPMediaLibraryAuthorizationStatusAuthorized
import platform.MediaPlayer.MPMediaQuery

class IosAlbumsData(): AlbumsData() {
    override fun albums(): List<Album> {
       if(MPMediaLibrary.authorizationStatus() != MPMediaLibraryAuthorizationStatusAuthorized){
           return emptyList()
       }

        val albums = mutableListOf<Album>()
        val mediaCollections = MPMediaQuery.albumsQuery().collections ?: return emptyList()

        for (collection in mediaCollections){
            val mediaCollection = collection as? MPMediaItemCollection
            if(mediaCollection == null){
                continue
            }
            val mediaItem = mediaCollection.representativeItem ?: continue
            val albumId = (mediaItem.valueForProperty(MPMediaItemPropertyAlbumPersistentID) as? NSNumber)?.longLongValue ?: 0
            val albumArt = "mediaitem-art://$albumId"
            albums += Album(
                albumId = albumId,
                title = mediaItem.valueForProperty(MPMediaItemPropertyAlbumTitle) as? String ?: "Album",
                artist = mediaItem.valueForProperty(MPMediaItemPropertyArtist) as? String ?: "Artist",
                albumArt = albumArt
            )
        }
        return albums.distinct()
    }
}