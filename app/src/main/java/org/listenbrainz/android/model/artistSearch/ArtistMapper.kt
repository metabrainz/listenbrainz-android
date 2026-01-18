package org.listenbrainz.android.model.artistSearch

import org.listenbrainz.android.model.search.artistSearch.ArtistUiModel

fun ArtistData.toUiModel(): ArtistUiModel {
    return ArtistUiModel(
        id = this.id,
        name = this.name,
        type = this.type ?: "Artist",
        gender = this.gender?.replaceFirstChar { it.uppercase() } ?: "Not specified",
        area = this.area?.name ?: "Unknown Area"
    )
}