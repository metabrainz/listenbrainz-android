package org.listenbrainz.android.model.artist

data class ArtistPayload(
    val artist: Artist? = null,
    val coverArt: String? = null,
    val listeningStats: ListeningStats? = null,
    val popularRecordings: List<PopularRecording?> = listOf(),
    val releaseGroups: List<ReleaseGroup?> = listOf(),
    val similarArtists: SimilarArtists? = null
)