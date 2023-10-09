package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.util.LinkedService

data class ListenBrainzExternalServices(
    val services: List<String>,
    @SerializedName("user_name") val userName: String,
) {
    fun toLinkedServicesList(): List<LinkedService> =
       services.map {
            LinkedService.parseService(it)
       }
    
}