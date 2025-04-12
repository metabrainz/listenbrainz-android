package org.listenbrainz.android.ui.screens.explore

import org.listenbrainz.android.model.explore.Release

data class HueSoundUiState(
    var releases: List<Release> = emptyList()
)