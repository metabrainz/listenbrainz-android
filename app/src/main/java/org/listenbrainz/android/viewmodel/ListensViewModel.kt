package org.listenbrainz.android.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.service.YouTubeApiService
import org.listenbrainz.android.util.Resource.Status.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    suspend fun searchYoutubeMusicVideoId(trackName: String, artist: String, apiKey: String): String? {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(YouTubeApiService::class.java)

        return try {
            val response = service.searchVideos(
                "snippet",
                "$trackName $artist",
                "video",
                "10",
                apiKey
            )

            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                if (items.isNotEmpty()) {
                    items[0].id.videoId
                } else {
                    null
                }
            } else {
                Log.e("YouTube API Error", response.errorBody()?.string() ?: "")
                null
            }
        } catch (e: Exception) {
            Log.e("YouTube API Error", "Error occurred while searching for video ID", e)
            null
        }
    }
}