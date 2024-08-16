package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Area(
                                        val disambiguation: String? = null,
                                        val id: String? = null,
    @SerializedName("iso-3166-1-codes") val isoCodes: List<String?>? = null,
                                        val name: String? = null,
    @SerializedName("sort-name")        val sortName: String? = null,
                                        val type: String? = null,
    @SerializedName("type-id")          val typeId: String? = null
)
