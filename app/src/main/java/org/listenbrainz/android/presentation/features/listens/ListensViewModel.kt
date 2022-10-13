package org.listenbrainz.android.presentation.features.listens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.ListensRepository
import org.listenbrainz.android.data.sources.api.entities.listens.Listen
import org.listenbrainz.android.util.Resource.Status.*
import javax.inject.Inject

@HiltViewModel
class ListensViewModel @Inject constructor(val repository: ListensRepository) : ViewModel() {
    var listens: List<Listen> by mutableStateOf(listOf())
    var isLoading: Boolean  by mutableStateOf(true)

    fun fetchUserListens(userName: String) {
        viewModelScope.launch {
            val response = repository.fetchUserListens(userName)
            when(response.status){
                SUCCESS -> {
                    isLoading = false
                    listens = response.data!!
                    listens.forEachIndexed { index, listen ->
                        var releaseMBID:String? = null
                        var spotifyImage: String? = null
                        if(spotifyImage!=null){
                            return@forEachIndexed
                        }
                        when {
                            listen.track_metadata.additional_info?.release_mbid != null -> {
                                releaseMBID = listen.track_metadata.additional_info.release_mbid
                            }
                            listen.track_metadata.mbid_mapping?.release_mbid != null -> {
                                releaseMBID = listen.track_metadata.mbid_mapping.release_mbid
                            }
                        }
                        val responseCoverArt = releaseMBID?.let { repository.fetchCoverArt(it) }
                        when(responseCoverArt?.status) {
                            SUCCESS -> {
                                listens[index].coverArt = responseCoverArt.data!!
                            }
                            else -> {

                            }
                        }
                    }
                }
                LOADING -> {
                    isLoading = true
                }
                FAILED -> {
                    isLoading = false
                }
            }
        }
    }
}