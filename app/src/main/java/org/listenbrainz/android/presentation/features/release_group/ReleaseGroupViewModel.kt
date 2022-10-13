package org.listenbrainz.android.presentation.features.release_group

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.api.entities.WikiSummary
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.data.sources.api.entities.mbentity.ReleaseGroup
import org.listenbrainz.android.presentation.features.base.LookupViewModel
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class ReleaseGroupViewModel @Inject constructor(repository: LookupRepository) :
        LookupViewModel<ReleaseGroup>(repository, MBEntityType.RELEASE_GROUP) {

    val wikiData: LiveData<Resource<WikiSummary>>
    override val data: LiveData<Resource<ReleaseGroup>> = jsonLiveData.map { parseData(it) }

    private suspend fun fetchWikiSummary(resource: Resource<ReleaseGroup>): Resource<WikiSummary> {
        if (resource.status == Resource.Status.SUCCESS) {
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
            if (title.isNotEmpty())
                return repository.fetchWikiSummary(title, method)
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