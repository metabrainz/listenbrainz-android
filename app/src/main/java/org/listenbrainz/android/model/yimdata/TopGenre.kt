package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class TopGenre(
    @SerializedName("genre" ) var genre     : String             = "",
    @SerializedName("genre_count"      ) var genreCount         : Number             = 0,
    @SerializedName("genre_count_percent"      ) var genreCountPercent      : Float   = 0f
)