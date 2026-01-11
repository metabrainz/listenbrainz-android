package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopGenre(
    @SerialName("genre") var genre: String = "",
    @SerialName("genre_count") var genreCount: Int = 0,
    @SerialName("genre_count_percent") var genreCountPercent: Float = 0f
)