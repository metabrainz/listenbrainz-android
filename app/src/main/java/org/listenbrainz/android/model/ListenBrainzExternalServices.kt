package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.LinkedService.Companion.toLinkedService

@Serializable
data class ListenBrainzExternalServices(
    val services: List<String> = emptyList(),
    @SerialName("user_name") val userName: String = "",
) {
    fun toLinkedServicesList(): List<LinkedService> =
       services.map {
            it.toLinkedService()
       }
    
}