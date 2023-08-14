package org.listenbrainz.android.model

data class SocialData (
    var followers: List<String>? = null,
    var following: List<String>? = null,
    val user: String? = null
)