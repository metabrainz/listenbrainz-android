package org.listenbrainz.android.presentation.features.label

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.api.entities.mbentity.Label
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.presentation.features.base.LookupViewModel
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(repository: LookupRepository) : LookupViewModel<Label>(repository, MBEntityType.LABEL) {
    override val data: LiveData<Resource<Label>> = jsonLiveData.map { parseData(it) }
}