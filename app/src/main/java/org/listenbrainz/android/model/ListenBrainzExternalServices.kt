package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.LinkedService.Companion.toLinkedService

data class ListenBrainzExternalServices(
    val services: List<String>,
    @SerializedName("user_name") val userName: String,
) {
    fun toLinkedServicesList(): List<LinkedService> =
       services.map {
            it.toLinkedService()
       }
    
}