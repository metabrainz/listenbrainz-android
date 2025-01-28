package org.listenbrainz.sharedtest.testdata

import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistPayload

object PlaylistDataRepositoryTestData {
    val playlistDetailsTestData: PlaylistPayload = PlaylistPayload(
        playlist = PlaylistData(
            annotation = "Description of the playlist",
            title = "Weekly playlist",
            date= "2025-01-12T11:05:24.966018+00:00",
            creator = "hemang-mishra"
        )
    )
}