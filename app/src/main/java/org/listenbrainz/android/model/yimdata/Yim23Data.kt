package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23Data (

    @SerializedName("artist_map"                       ) var artistMap                 : ArrayList<ArtistMap>               = arrayListOf(),
    @SerializedName("day_of_week"                      ) var dayOfWeek                 : String                             = "",
    @SerializedName("listens_per_day"                  ) var listensPerDay             : ArrayList<ListensPerDay>           = arrayListOf(),
    @SerializedName("most_listened_year"               ) var mostListenedYear          : Map<String, Int>                   = mapOf(),
    @SerializedName("new_releases_of_top_artists"      ) var newReleasesOfTopArtists   : ArrayList<NewReleasesOfTopArtist>  = arrayListOf(),

    // This map is null if user has no similar users, i.e., has less listen time.
    @SerializedName("similar_users"                    ) var similarUsers              : Map<String, Double>?               = null,

    @SerializedName("top_artists"                      ) var topArtists                : ArrayList<TopArtist>               = arrayListOf(),
    @SerializedName("top_genres"                      ) var topGenres                  : ArrayList<TopGenre>               = arrayListOf(),
    @SerializedName("top_recordings"                   ) var topRecordings             : ArrayList<TopRecording>            = arrayListOf(),
    @SerializedName("top_release_groups"                     ) var topReleases               : ArrayList<TopReleaseYim23>              = arrayListOf(),
    @SerializedName("total_artists_count"              ) var totalArtistsCount         : Int                                = 0,
    @SerializedName("total_listen_count"               ) var totalListenCount          : Int                                = 0,       // This field identifies if user has listened to at least 1 song
    @SerializedName("total_listening_time"             ) var totalListeningTime        : Double                             = 0.0,      // Checking is done through Type adapters.
    @SerializedName("total_new_artists_discovered"     ) var totalNewArtistsDiscovered : Int                                = 0,
    @SerializedName("total_recordings_count"           ) var totalRecordingsCount      : Int                                = 0,
    @SerializedName("total_releases_count"             ) var totalReleasesCount        : Int                                = 0,

    @SerializedName("top-discoveries"                ) var topDiscoveriesPlaylist         : Yim23TopDiscoveries = Yim23TopDiscoveries(),
    @SerializedName("top-missed-recordings"                ) var topMissedRecordings         : Yim23TopDiscoveries = Yim23TopDiscoveries()

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