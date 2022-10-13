package org.listenbrainz.android.presentation.features.artist

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.Artist
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType.ARTIST
import org.listenbrainz.android.presentation.features.base.LookupViewModel
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(repository: LookupRepository) : LookupViewModel<Artist>(repository, ARTIST) {

    val wikiData: LiveData<Resource<WikiSummary>>

    override val data: LiveData<Resource<Artist>> = jsonLiveData.map { parseData(it) }

    private suspend fun fetchWikiSummary(resource: Resource<Artist>): Resource<WikiSummary> {
        if (resource.status == SUCCESS) {
            var title = ""
            var method = -1
            for (link in resource.data!!.relations) {
                if (link.type == "wikipedia") {
                    title = link.pageTitle
                    method = LookupRepository.METHOD_WIKIPEDIA_URL
                    break
                }
                if (link.type == "wikidata") {
                    title = link.pageTitle
                    method = LookupRepository.METHOD_WIKIDATA_ID
                    break
                }
            }
            if (title.isNotEmpty()) {
                return repository.fetchWikiSummary(title, method)
            }
        }
        return Resource.failure()
    }

    init {
        wikiData = data.switchMap {
            liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                emit(fetchWikiSummary(it))
            }
        }
    }

}