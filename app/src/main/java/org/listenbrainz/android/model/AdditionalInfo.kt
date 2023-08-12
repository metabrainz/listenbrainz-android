package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class AdditionalInfo(
    @SerializedName("artist_msid"              ) val artistMsid: String? = null,
    @SerializedName("artist_names"             ) val artistNames: List<String>? = null,
    @SerializedName("discnumber"               ) val discNumber: Int? = null,
    @SerializedName("duration_ms"              ) var durationMs: Int? = null,
    @SerializedName("isrc"                     ) val isrc: String? = null,
    @SerializedName("listening_from"           ) val listeningFrom: String? = null,
    @SerializedName("recording_msid"           ) val recordingMsid: String? = null,
    @SerializedName("release_artist_name"      ) val releaseArtistName: String? = null,
    @SerializedName("release_artist_names"     ) val releaseArtistNames: List<String>? = null,
    @SerializedName("release_msid"             ) val releaseMsid: String? = null,
    @SerializedName("release_mbid"             ) val releaseMbid: String? = null,
    @SerializedName("spotify_album_artist_ids" ) val spotifyAlbumArtistIds: List<String>? = null,
    @SerializedName("spotify_album_id"         ) val spotifyAlbumId: String? = null,
    @SerializedName("spotify_artist_ids"       ) val spotifyArtistIds: List<String>? = null,
    @SerializedName("spotify_id"               ) val spotifyId: String? = null,
    @SerializedName("tracknumber"              ) val trackNumber: Int? = null,
    
    // Used for listen submission body
    @SerializedName("media_player"             ) val mediaPlayer: String? = null,
    @SerializedName("submission_client"        ) val submissionClient: String? = null,
    @SerializedName("submission_client_version") val submissionClientVersion: String? = null,

    // Feed Specific
    @SerializedName("artist_mbids"             ) val artistMbids: List<String>? = null,
    @SerializedName("origin_url"               ) val originUrl: String? = null,
    @SerializedName("recording_mbid"           ) val recordingMbid: String? = null,
    @SerializedName("release_group_mbid"       ) val releaseGroupMbid: String? = null,
    //@SerializedName("tags"                     ) val tags: Any? = null,
    //@SerializedName("track_mbid"               ) val trackMbid: Any? = null,
    //@SerializedName("work_mbids"               ) val workMbids: Any? = null,
    //@SerializedName("youtube_id"               ) val youtubeId: Any? = null
)