package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ArtistBio(
                                 val area: Area? = null,
                                 val country: String? = null,
                                 val id: String? = null,
    @SerializedName("life-span") val lifeSpan: LifeSpan? = null,
                                 val name: String? = null,
    @SerializedName("sort-name") val sortName: String? = null,
)
