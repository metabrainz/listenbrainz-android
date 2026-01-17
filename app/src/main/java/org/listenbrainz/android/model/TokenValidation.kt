package org.listenbrainz.android.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class TokenValidation(
    val code: Int = 0,
    val message: String = "",
    @SerialName("user_name") val username: String? = null,
    val valid: Boolean = false
)