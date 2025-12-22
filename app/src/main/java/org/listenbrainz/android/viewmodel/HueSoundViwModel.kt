package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.explore.Release
import org.listenbrainz.android.repository.explore.ExploreRepository
import org.listenbrainz.android.ui.screens.explore.HueSoundUiState
import javax.inject.Inject

@HiltViewModel
class HueSoundViwModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : BaseViewModel<HueSoundUiState>() {
    private val releaseList = MutableStateFlow(listOf<Release>())
    private val _selectedRelease = MutableStateFlow(Release())
    override val uiState: StateFlow<HueSoundUiState> = createUiStateFlow()


    fun changeSelectedRelease(release: Release) {
        _selectedRelease.value = release
    }

    override fun createUiStateFlow(): StateFlow<HueSoundUiState> =
        combine(releaseList, _selectedRelease) { releases, selected ->
            HueSoundUiState(releases = releases, selectedRelease = selected)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HueSoundUiState()
        )

    fun onColorPickled(colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            releaseList.value =
                exploreRepository.getReleasesFromColor(colorHex).data?.payload?.releases
                    ?: emptyList()
        }
    }
}