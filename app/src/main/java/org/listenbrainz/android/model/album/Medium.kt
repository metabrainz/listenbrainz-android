package org.listenbrainz.android.model.album

data class Medium(
    val format: String? = null,
    val name: String? = null,
    val position: Int? = null,
    val tracks: List<Track?>? = null
)