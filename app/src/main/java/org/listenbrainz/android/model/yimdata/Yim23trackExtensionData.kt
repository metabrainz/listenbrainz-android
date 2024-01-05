package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23TrackExtensionData (
    @SerializedName("added_at") var addedAt    : String   = "",
    @SerializedName("added_by") var addedBy    : String   = "",
    @SerializedName("additional_metadata") var additionalMetadata   : Yim23AdditionalMetadata   = Yim23AdditionalMetadata(),

    )
