package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewMetadata(
    
    @SerialName("entity_name") val entityName: String,
    /** **Mbid** of the entity.*/
    @SerialName("entity_id") val entityId: String,
    /**"**recording**" or "**artist**" or "**release_group**"*/
    @SerialName("entity_type") val entityType: String,
    
    /** Text should be minimum 25 characters, error should be thrown if not. */
    @SerialName("text") val text: String = "",
    @SerialName("language") val language: String = "en",
    
    /** Rating should lie between 1..5 */
    @SerialName("rating") val rating: Int? = null

)