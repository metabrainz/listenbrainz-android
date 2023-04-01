package org.listenbrainz.android.model

data class onlinePlaylistDetails(
    val count: Int,
    val offset: Int,
    val playlist_count: Int,
    val playlists: List<onlinePlaylist>
)

data class onlinePlaylist(
    val playlist: PlaylistDetails
)

data class PlaylistDetails(
    val creator: String,
    val date: String,
    val extension: Extension,
    val identifier: String,
    val title: String,
    val track: List<Track>
)

data class Extension(
    val playlist: PlaylistExtension
)

data class PlaylistExtension(
    val creator: String,
    val last_modified_at: String,
    val public: Boolean
)

data class PlaylistResponse(
    val playlist: songPlaylist
)

data class songPlaylist(
    val creator: String,
    val date: String,
    val extension: musicBrainzExtension,
    val identifier: String,
    val title: String,
    val track: List<Track>
)

data class musicBrainzExtension(
    val musicBrainz: MusicBrainz
)

data class MusicBrainz(
    val creator: String,
    val lastModifiedAt: String,
    val public: Boolean
)

data class Track(
    val creator: String,
    val extension: TrackExtension?,
    val identifier: String,
    val title: String
)

data class TrackExtension(
    val addedAt: String,
    val addedBy: String,
    val additionalMetadata: AdditionalMetadata?,
    val artistIdentifiers: List<String>
)

data class AdditionalMetadata(
    val caaId: Long,
    val caaReleaseMbid: String
)