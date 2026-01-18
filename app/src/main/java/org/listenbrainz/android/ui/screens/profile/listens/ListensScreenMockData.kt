package org.listenbrainz.android.ui.screens.profile.listens

import androidx.paging.PagingData
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.shared.model.AdditionalInfo
import org.listenbrainz.android.model.Listen
import org.listenbrainz.shared.model.MbidMapping
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.shared.model.TrackMetadata
import org.listenbrainz.shared.model.feed.FeedListenArtist
import org.listenbrainz.android.model.user.Artist
import org.listenbrainz.android.ui.screens.profile.ListensTabUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState

/**
 * Mock data for ListensScreen previews and testing
 */
object ListensScreenMockData {

    // Helper function to create MbidMapping
    private fun createMbidMapping(
        recordingMbid: String,
        recordingName: String,
        artistMbids: List<String>,
        caaReleaseMbid: String? = null,
        caaId: Long? = null
    ) = MbidMapping(
        recordingMbid = recordingMbid,
        recordingName = recordingName,
        artistMbids = artistMbids,
        caaReleaseMbid = caaReleaseMbid,
        caaId = caaId,
        releaseMbid = caaReleaseMbid,
        artists = artistMbids.mapIndexed { index, mbid ->
            FeedListenArtist(
                artistCreditName = mockArtists.getOrNull(index)?.artistName ?: "Unknown Artist",
                artistMbid = mbid,
                joinPhrase = if (index < artistMbids.size - 1) ", " else ""
            )
        }
    )

    // Helper function to create AdditionalInfo
    private fun createAdditionalInfo(
        durationMs: Int? = null,
        spotifyId: String? = null
    ) = AdditionalInfo(
        durationMs = durationMs,
        spotifyId = spotifyId,
        listeningFrom = "ListenBrainz Android",
        submissionClient = "ListenBrainz Android",
        submissionClientVersion = "1.0.0"
    )

    // Mock Artists
    val mockArtists get() = listOf(
        Artist(
            artistMbid = "5b11f4ce-a62d-471e-81fc-a69a8278c7da",
            artistName = "Nirvana",
            listenCount = 542
        ),
        Artist(
            artistMbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
            artistName = "Radiohead",
            listenCount = 423
        ),
        Artist(
            artistMbid = "cc197bad-dc9c-440d-a5b5-d52ba2e14234",
            artistName = "Coldplay",
            listenCount = 389
        ),
        Artist(
            artistMbid = "9c9f1380-2516-4fc9-a3e6-f9f61941d090",
            artistName = "Muse",
            listenCount = 312
        ),
        Artist(
            artistMbid = "8bfac288-ccc5-448d-9573-c33ea2aa5c30",
            artistName = "Red Hot Chili Peppers",
            listenCount = 287
        ),
        Artist(
            artistMbid = "83d91898-7763-47d7-b03b-b92132375c47",
            artistName = "Pink Floyd",
            listenCount = 265
        )
    )

    // Mock Similar Users
    val mockSimilarUsers get() = listOf(
        SimilarUser(similarity = 0.85, username = "musiclover123"),
        SimilarUser(similarity = 0.78, username = "rockenthusiast"),
        SimilarUser(similarity = 0.72, username = "indie_vibes"),
        SimilarUser(similarity = 0.68, username = "vinyl_collector"),
        SimilarUser(similarity = 0.65, username = "alternative_rock_fan"),
        SimilarUser(similarity = 0.61, username = "grunge_era"),
        SimilarUser(similarity = 0.58, username = "classic_rock_head")
    )

    // Mock Followers
    val mockFollowers get() = listOf(
        Pair("alice_music", true),
        Pair("bob_listener", false),
        Pair("charlie_beats", true),
        Pair("diana_sounds", false),
        Pair("eve_melodies", true),
        Pair("frank_tunes", false),
        Pair("grace_harmony", true)
    )

    // Mock Following
    val mockFollowing get() = listOf(
        Pair("john_indie", true),
        Pair("sarah_rock", true),
        Pair("mike_jazz", true),
        Pair("emma_pop", true),
        Pair("oliver_metal", true),
        Pair("sophia_folk", true),
        Pair("james_electronic", true)
    )

    // Mock Listens
    val mockListens get() = listOf(
        Listen(
            insertedAt = 1704067200,
            listenedAt = 1704067200,
            recordingMsid = "d8b663dc-374e-4436-9448-da92dedef3ce",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Smells Like Teen Spirit",
                artistName = "Nirvana",
                releaseName = "Nevermind",
                mbidMapping = createMbidMapping(
                    recordingMbid = "b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1",
                    recordingName = "Smells Like Teen Spirit",
                    artistMbids = listOf("5b11f4ce-a62d-471e-81fc-a69a8278c7da"),
                    caaReleaseMbid = "1b022e01-4da6-387b-8658-8678046e4cef",
                    caaId = 12345678901234L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 301920,
                    spotifyId = "4CeeEOM32jQcH3eN9Q2dGj"
                )
            )
        ),
        Listen(
            insertedAt = 1704063600,
            listenedAt = 1704063600,
            recordingMsid = "a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Creep",
                artistName = "Radiohead",
                releaseName = "Pablo Honey",
                mbidMapping = createMbidMapping(
                    recordingMbid = "c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2",
                    recordingName = "Creep",
                    artistMbids = listOf("a74b1b7f-71a5-4011-9441-d0b5e4122711"),
                    caaReleaseMbid = "2c2c2c2c-2c2c-2c2c-2c2c-2c2c2c2c2c2c",
                    caaId = 23456789012345L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 238400,
                    spotifyId = "70LcF31zb1H0PyJoS1Sx1r"
                )
            )
        ),
        Listen(
            insertedAt = 1704060000,
            listenedAt = 1704060000,
            recordingMsid = "e3e3e3e3-e3e3-e3e3-e3e3-e3e3e3e3e3e3",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Fix You",
                artistName = "Coldplay",
                releaseName = "X&Y",
                mbidMapping = createMbidMapping(
                    recordingMbid = "f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4",
                    recordingName = "Fix You",
                    artistMbids = listOf("cc197bad-dc9c-440d-a5b5-d52ba2e14234"),
                    caaReleaseMbid = "4f4f4f4f-4f4f-4f4f-4f4f-4f4f4f4f4f4f",
                    caaId = 34567890123456L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 295346,
                    spotifyId = "7LVHVU3tWfcxj5aiPFEW4Q"
                )
            )
        ),
        Listen(
            insertedAt = 1704056400,
            listenedAt = 1704056400,
            recordingMsid = "g5g5g5g5-g5g5-g5g5-g5g5-g5g5g5g5g5g5",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Starlight",
                artistName = "Muse",
                releaseName = "Black Holes and Revelations",
                mbidMapping = createMbidMapping(
                    recordingMbid = "h6h6h6h6-h6h6-h6h6-h6h6-h6h6h6h6h6h6",
                    recordingName = "Starlight",
                    artistMbids = listOf("9c9f1380-2516-4fc9-a3e6-f9f61941d090"),
                    caaReleaseMbid = "6h6h6h6h-6h6h-6h6h-6h6h-6h6h6h6h6h6h",
                    caaId = 45678901234567L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 240733,
                    spotifyId = "3skn2lauGk7Dx6bVIt5DVj"
                )
            )
        ),
        Listen(
            insertedAt = 1704052800,
            listenedAt = 1704052800,
            recordingMsid = "i7i7i7i7-i7i7-i7i7-i7i7-i7i7i7i7i7i7",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Under the Bridge",
                artistName = "Red Hot Chili Peppers",
                releaseName = "Blood Sugar Sex Magik",
                mbidMapping = createMbidMapping(
                    recordingMbid = "j8j8j8j8-j8j8-j8j8-j8j8-j8j8j8j8j8j8",
                    recordingName = "Under the Bridge",
                    artistMbids = listOf("8bfac288-ccc5-448d-9573-c33ea2aa5c30"),
                    caaReleaseMbid = "8j8j8j8j-8j8j-8j8j-8j8j-8j8j8j8j8j8j",
                    caaId = 56789012345678L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 264826,
                    spotifyId = "3d9DChrdc6BOeFsbrZ3Is0"
                )
            )
        ),
        Listen(
            insertedAt = 1704049200,
            listenedAt = 1704049200,
            recordingMsid = "k9k9k9k9-k9k9-k9k9-k9k9-k9k9k9k9k9k9",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Comfortably Numb",
                artistName = "Pink Floyd",
                releaseName = "The Wall",
                mbidMapping = createMbidMapping(
                    recordingMbid = "l0l0l0l0-l0l0-l0l0-l0l0-l0l0l0l0l0l0",
                    recordingName = "Comfortably Numb",
                    artistMbids = listOf("83d91898-7763-47d7-b03b-b92132375c47"),
                    caaReleaseMbid = "0l0l0l0l-0l0l-0l0l-0l0l-0l0l0l0l0l0l",
                    caaId = 67890123456789L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 382893,
                    spotifyId = "5HNCy40Ni5BZJFw1TKzRsC"
                )
            )
        ),
        Listen(
            insertedAt = 1704045600,
            listenedAt = 1704045600,
            recordingMsid = "m1m1m1m1-m1m1-m1m1-m1m1-m1m1m1m1m1m1",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Come As You Are",
                artistName = "Nirvana",
                releaseName = "Nevermind",
                mbidMapping = createMbidMapping(
                    recordingMbid = "n2n2n2n2-n2n2-n2n2-n2n2-n2n2n2n2n2n2",
                    recordingName = "Come As You Are",
                    artistMbids = listOf("5b11f4ce-a62d-471e-81fc-a69a8278c7da"),
                    caaReleaseMbid = "1b022e01-4da6-387b-8658-8678046e4cef",
                    caaId = 12345678901234L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 219200,
                    spotifyId = "1RxvUTlJ6yuFm0I0RXVAhv"
                )
            )
        ),
        Listen(
            insertedAt = 1704042000,
            listenedAt = 1704042000,
            recordingMsid = "o3o3o3o3-o3o3-o3o3-o3o3-o3o3o3o3o3o3",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Karma Police",
                artistName = "Radiohead",
                releaseName = "OK Computer",
                mbidMapping = createMbidMapping(
                    recordingMbid = "p4p4p4p4-p4p4-p4p4-p4p4-p4p4p4p4p4p4",
                    recordingName = "Karma Police",
                    artistMbids = listOf("a74b1b7f-71a5-4011-9441-d0b5e4122711"),
                    caaReleaseMbid = "4p4p4p4p-4p4p-4p4p-4p4p-4p4p4p4p4p4p",
                    caaId = 78901234567890L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 264160,
                    spotifyId = "63OQupATfueTdZMWTxW03A"
                )
            )
        ),
        Listen(
            insertedAt = 1704038400,
            listenedAt = 1704038400,
            recordingMsid = "q5q5q5q5-q5q5-q5q5-q5q5-q5q5q5q5q5q5",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Viva la Vida",
                artistName = "Coldplay",
                releaseName = "Viva la Vida or Death and All His Friends",
                mbidMapping = createMbidMapping(
                    recordingMbid = "r6r6r6r6-r6r6-r6r6-r6r6-r6r6r6r6r6r6",
                    recordingName = "Viva la Vida",
                    artistMbids = listOf("cc197bad-dc9c-440d-a5b5-d52ba2e14234"),
                    caaReleaseMbid = "6r6r6r6r-6r6r-6r6r-6r6r-6r6r6r6r6r6r",
                    caaId = 89012345678901L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 242093,
                    spotifyId = "1mea3bSkSGXuIRvnydlB5b"
                )
            )
        ),
        Listen(
            insertedAt = 1704034800,
            listenedAt = 1704034800,
            recordingMsid = "s7s7s7s7-s7s7-s7s7-s7s7-s7s7s7s7s7s7",
            userName = "pranavkonidena",
            trackMetadata = TrackMetadata(
                trackName = "Uprising",
                artistName = "Muse",
                releaseName = "The Resistance",
                mbidMapping = createMbidMapping(
                    recordingMbid = "t8t8t8t8-t8t8-t8t8-t8t8-t8t8t8t8t8t8",
                    recordingName = "Uprising",
                    artistMbids = listOf("9c9f1380-2516-4fc9-a3e6-f9f61941d090"),
                    caaReleaseMbid = "8t8t8t8t-8t8t-8t8t-8t8t-8t8t8t8t8t8t",
                    caaId = 90123456789012L
                ),
                additionalInfo = createAdditionalInfo(
                    durationMs = 304626,
                    spotifyId = "2takcwOaAZWiXQijPHIx7B"
                )
            )
        )
    )

    // Complete mock with all data
    val mockProfileUiState get() = ProfileUiState(
        isSelf = false,
        listensTabUiState = ListensTabUiState(
            isLoading = false,
            listenCount = 8932,
            followersCount = 18,
            followingCount = 32,
            recentListens = flow { emit(PagingData.from(mockListens)) },
            followers = mockFollowers,
            following = mockFollowing,
            similarUsers = mockSimilarUsers,
            similarArtists = mockArtists,
            compatibility = 0.73f,
            isFollowing = false
        )
    )

    // Self profile variant
    val mockProfileUiStateSelf get() = mockProfileUiState.copy(
        isSelf = true,
        listensTabUiState = mockProfileUiState.listensTabUiState.copy(
            listenCount = 15847,
            followersCount = 23,
            followingCount = 45,
            compatibility = null
        )
    )

    // Following state variant
    val mockProfileUiStateFollowing get() = mockProfileUiState.copy(
        listensTabUiState = mockProfileUiState.listensTabUiState.copy(
            isFollowing = true
        )
    )

    // User with no data
    val mockProfileUiStateNoData get() = ProfileUiState(
        isSelf = false,
        listensTabUiState = ListensTabUiState(
            isLoading = false,
            listenCount = 0,
            followersCount = 0,
            followingCount = 0,
            recentListens = emptyFlow(),
            followers = emptyList(),
            following = emptyList(),
            similarUsers = emptyList(),
            similarArtists = emptyList(),
            compatibility = 0f,
            isFollowing = false
        )
    )

    // Convenience aliases for backward compatibility
    val mockProfileUiStateOther get() = mockProfileUiState
    val mockProfileUiStateMinimal get() = mockProfileUiState.copy(
        listensTabUiState = mockProfileUiState.listensTabUiState.copy(
            listenCount = 42,
            followersCount = 0,
            followingCount = 1,
            recentListens = flow { emit(PagingData.from(mockListens.take(3))) },
            followers = emptyList(),
            following = listOf(Pair("music_admin", true)),
            similarUsers = emptyList(),
            similarArtists = emptyList(),
            compatibility = 0.12f
        )
    )

    val mockPreferencesUiState get() = PreferencesUiState(
        isSpotifyLinked = true,
        username = "pranavkonidena",
        accessToken = "mock_access_token_12345",
        isNotificationServiceAllowed = true,
        listeningWhitelist = listOf("Spotify", "YouTube Music", "Apple Music"),
        listeningApps = listOf("Spotify", "YouTube Music")
    )

    val mockSocialUiState get() = SocialUiState(
        searchResult = emptyList(),
        error = null,
        successMsgId = null
    )
}
