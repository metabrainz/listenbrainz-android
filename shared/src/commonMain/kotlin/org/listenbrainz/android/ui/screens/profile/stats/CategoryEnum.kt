package org.listenbrainz.android.ui.screens.profile.stats

import org.jetbrains.compose.resources.StringResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.album_title
import org.listenbrainz.shared.resources.artist_title
import org.listenbrainz.shared.resources.songs_title

enum class CategoryState(val text: StringResource) {
    ARTISTS(Res.string.artist_title),
    ALBUMS(Res.string.album_title),
    SONGS(Res.string.songs_title)
}
