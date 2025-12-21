package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23TrackExtensionData (
    @SerialName("added_at") var addedAt: String = "",
    @SerialName("added_by") var addedBy: String = "",
    @SerialName("additional_metadata") var additionalMetadata: Yim23AdditionalMetadata = Yim23AdditionalMetadata(),
)
