package org.listenbrainz.android.ui.screens.explore

import org.listenbrainz.android.model.explore.Release

data class HueSoundUiState(
    val releases: List<Release> = emptyList(),
    val selectedRelease: Release = Release()
)