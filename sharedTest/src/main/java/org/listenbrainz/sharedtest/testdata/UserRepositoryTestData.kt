package org.listenbrainz.sharedtest.testdata

import kotlinx.serialization.json.Json
import org.listenbrainz.shared.model.CurrentPins
import org.listenbrainz.shared.model.Listens
import org.listenbrainz.shared.model.Payload
import org.listenbrainz.shared.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.shared.model.userPlaylist.UserPlaylist
import org.listenbrainz.shared.model.userPlaylist.UserPlaylists
import org.listenbrainz.shared.model.user.AllPinnedRecordings
import org.listenbrainz.shared.model.user.TopAlbums
import org.listenbrainz.shared.model.user.TopArtists
import org.listenbrainz.shared.model.user.TopSongs
import org.listenbrainz.shared.model.user.UserFeedback
import org.listenbrainz.shared.model.user.UserListeningActivity
import org.listenbrainz.shared.model.user.UserSimilarity
import org.listenbrainz.shared.model.user.UserSimilarityPayload
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