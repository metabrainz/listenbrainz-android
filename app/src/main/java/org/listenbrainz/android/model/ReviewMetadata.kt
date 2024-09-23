package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class ReviewMetadata(
    
    @SerializedName("entity_name" ) var entityName : String,
    /** **Mbid** of the entity.*/
    @SerializedName("entity_id"   ) var entityId   : String,
    /**"**recording**" or "**artist**" or "**release_group**"*/
    @SerializedName("entity_type" ) var entityType : String,
    
    /** Text should be minimum 25 characters, error should be thrown if not. */
    @SerializedName("text"        ) var text       : String,
    @SerializedName("language"    ) var language   : String = "en",
    
    /** Rating should lie between 1..5 */
    @SerializedName("rating"      ) var rating     : Int? = null

)