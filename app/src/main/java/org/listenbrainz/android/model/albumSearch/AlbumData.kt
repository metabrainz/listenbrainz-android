package org.listenbrainz.android.model.albumSearch


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.recordingSearch.ArtistCredit

@Serializable
data class AlbumData(
    @SerialName("id")
    val id: String? = null,
    @SerialName("score")
    val score: String? = null,
    @SerialName("count")
    val releaseCount: Int? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("first-release-date")
    val firstReleaseDate: String? = null,
    @SerialName("primary-type")
    val primaryType: String? = null,
    @SerialName("secondary-types")
    val secondaryTypes: List<String>? = null,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit> = emptyList(),
    @SerialName("releases")
    val releases: List<AlbumRelease> = emptyList(),
    @SerialName("tags")
    val tags: List<AlbumTag>? = null,
    @SerialName("disambiguation")
    val comment: String? = null
)
