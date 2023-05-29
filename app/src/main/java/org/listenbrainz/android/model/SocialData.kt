package org.listenbrainz.android.model

data class SocialData(
    val code: Int? = null,
    val error: String? = null,
    var followers: List<String>? = null,
    var following: List<String>? = null,
    val user: String
)