package org.listenbrainz.android.ui.screens.profile.stats

import androidx.annotation.StringRes
import org.listenbrainz.android.R

enum class CategoryState(@StringRes val text: Int) {
    ARTISTS(R.string.artist_title),
    ALBUMS(R.string.album_title),
    SONGS(R.string.songs_title)
}