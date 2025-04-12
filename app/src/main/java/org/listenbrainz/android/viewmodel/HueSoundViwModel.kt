package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
) :
    BaseViewModel<HueSoundUiState>() {
    private val releaseList = MutableStateFlow(listOf<Release>())
    override val uiState: StateFlow<HueSoundUiState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<HueSoundUiState> =
        combine(
            releaseList
        ) { array ->
            HueSoundUiState(releases = array[0])
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HueSoundUiState()
        )

    fun onColorPickled(colorHex: String) {
        viewModelScope.launch {
            releaseList.value =
                exploreRepository.getReleasesFromColor(colorHex).data?.payload?.releases
                    ?: emptyList()
        }
    }
}