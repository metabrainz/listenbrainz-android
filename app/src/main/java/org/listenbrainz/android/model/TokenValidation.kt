package org.listenbrainz.android.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TokenValidation(
    val code: Int,
    val message: String,
    @SerializedName("user_name") val username: String? = null,
    val valid: Boolean
)