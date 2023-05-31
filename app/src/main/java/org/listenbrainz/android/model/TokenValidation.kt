package org.listenbrainz.android.model

data class TokenValidation(
    val code: Int,
    val message: String,
    val user_name: String? = null,
    val valid: Boolean
)