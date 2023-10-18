package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class TokenValidation(
    val code: Int,
    val message: String,
    @SerializedName("user_name") val username: String? = null,
    val valid: Boolean
)