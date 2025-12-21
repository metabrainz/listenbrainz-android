package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val approved: Boolean = false,
    val back: Boolean = false,
    val comment: String = "",
    val edit: Int = 0,
    val front: Boolean = false,
    val id: String = "",
    val image: String = "",
    val thumbnails: Thumbnails = Thumbnails(),
    val types: List<String> = emptyList()
)