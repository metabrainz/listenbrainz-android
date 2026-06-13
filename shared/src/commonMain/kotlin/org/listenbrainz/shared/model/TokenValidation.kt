package org.listenbrainz.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenValidation(
    val code: Int = 0,
    val message: String = "",
    @SerialName("user_name") val username: String? = null,
    val valid: Boolean = false
)