package org.listenbrainz.android.presentation.features.release

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.presentation.features.base.LookupViewModel
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ReleaseViewModel @Inject constructor(repository: LookupRepository) :
        LookupViewModel<Release>(repository, MBEntityType.RELEASE) {

    val coverArtData: LiveData<CoverArt?> = mbid.switchMap {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = repository.fetchCoverArt(it)
            if (result.status == SUCCESS) {
                emit(result.data)
            }
        }
    }

    override val data: LiveData<Resource<Release>> = jsonLiveData.map { parseData(it) }

}