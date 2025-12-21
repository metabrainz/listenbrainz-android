package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23Data (
    @SerialName("artist_map") var artistMap: ArrayList<ArtistMap> = arrayListOf(),
    @SerialName("day_of_week") var dayOfWeek: String = "",
    @SerialName("listens_per_day") var listensPerDay: ArrayList<ListensPerDay> = arrayListOf(),
    @SerialName("most_listened_year") var mostListenedYear: Map<String, Int> = mapOf(),
    @SerialName("new_releases_of_top_artists") var newReleasesOfTopArtists: ArrayList<NewReleasesOfTopArtist> = arrayListOf(),

    // This map is null if user has no similar users, i.e., has less listen time.
    @SerialName("similar_users") var similarUsers: Map<String, Double>? = null,
    @SerialName("top_artists") var topArtists: ArrayList<TopArtist> = arrayListOf(),
    @SerialName("top_genres") var topGenres: ArrayList<TopGenre> = arrayListOf(),
    @SerialName("top_recordings") var topRecordings: ArrayList<TopRecording> = arrayListOf(),
    @SerialName("top_release_groups") var topReleases: ArrayList<TopReleaseYim23> = arrayListOf(),
    @SerialName("total_artists_count") var totalArtistsCount: Int = 0,
    @SerialName("total_listen_count") var totalListenCount: Int = 0,       // This field identifies if user has listened to at least 1 song
    @SerialName("total_listening_time") var totalListeningTime: Double = 0.0,      // Checking is done through Type adapters.
    @SerialName("total_new_artists_discovered") var totalNewArtistsDiscovered: Int = 0,
    @SerialName("total_recordings_count") var totalRecordingsCount: Int = 0,
    @SerialName("total_releases_count") var totalReleasesCount: Int = 0,
    @SerialName("playlist-top-discoveries-for-year") var topDiscoveriesPlaylist: Yim23TopDiscoveriesPlaylist = Yim23TopDiscoveriesPlaylist(),
    @SerialName("playlist-top-missed-recordings-for-year") var topMissedRecordings: Yim23TopDiscoveriesPlaylist = Yim23TopDiscoveriesPlaylist()
)

/**
For a new user : Json Structure will look like:

{
"payload": {
"data": {},
"user_name": "jasjeettest"
}
}

 */