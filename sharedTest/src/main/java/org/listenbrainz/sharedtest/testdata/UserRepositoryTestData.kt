package org.listenbrainz.sharedtest.testdata

import kotlinx.serialization.json.Json
import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.Payload
import org.listenbrainz.android.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.model.userPlaylist.UserPlaylists
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
import org.listenbrainz.android.model.user.UserSimilarity
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.sharedtest.utils.ResourceString.all_pins
import org.listenbrainz.sharedtest.utils.ResourceString.current_pins
import org.listenbrainz.sharedtest.utils.ResourceString.globalListeningActivity
import org.listenbrainz.sharedtest.utils.ResourceString.loved_hated_songs
import org.listenbrainz.sharedtest.utils.ResourceString.topAlbums
import org.listenbrainz.sharedtest.utils.ResourceString.topSongs
import org.listenbrainz.sharedtest.utils.ResourceString.top_artists
import org.listenbrainz.sharedtest.utils.ResourceString.userListeningActivity

private val json = Json { 
    ignoreUnknownKeys = true 
    coerceInputValues = true
    isLenient = true
}

object UserRepositoryTestData {
    val listenCountTestData: Listens = Listens(
        payload = Payload(
            count = 3252,
            latestListenTs = 0,
            listens = listOf(),
            userId = "Jasjeet"
        )
    )

    val createdForYouPlaylistsTestData: UserPlaylistPayload = UserPlaylistPayload(
        playlists = listOf(
            UserPlaylists(
                playlist = UserPlaylist(
                title = "Playlist 1",
                )
            )
        )
    )

    val userSimilarityTestData: UserSimilarityPayload = UserSimilarityPayload(
        userSimilarity = UserSimilarity(
            similarity = 0.2580655f,
            username = "Shubhi"
        )
    )

    val currentPinsTestData: CurrentPins?
        get() = json.decodeFromString<CurrentPins>(current_pins)

    val allPinsTestData: AllPinnedRecordings?
        get() = json.decodeFromString<AllPinnedRecordings>(all_pins)

    val topArtistsTestData: TopArtists
        get() = json.decodeFromString<TopArtists>(top_artists)

    val lovedHatedSongsTestData: UserFeedback
        get() = json.decodeFromString<UserFeedback>(loved_hated_songs)

    val listeningActivityTestData: UserListeningActivity
        get() = json.decodeFromString<UserListeningActivity>(userListeningActivity)

    val globalListeningActivityTestData: UserListeningActivity
        get() = json.decodeFromString<UserListeningActivity>(globalListeningActivity)

    val topAlbumsTestData: TopAlbums
        get() = json.decodeFromString<TopAlbums>(topAlbums)

    val topSongsTestData: TopSongs
        get() = json.decodeFromString<TopSongs>(topSongs)
}