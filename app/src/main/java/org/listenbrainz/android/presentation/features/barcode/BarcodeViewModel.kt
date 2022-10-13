package org.listenbrainz.android.presentation.features.barcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.data.repository.BarcodeLookupRepository
import org.listenbrainz.android.data.sources.api.entities.mbentity.Release
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class BarcodeViewModel @Inject constructor(private var barcodeLookupRepository: BarcodeLookupRepository) : ViewModel() {

    fun fetchReleasesWithBarcode(barcode: String): LiveData<Resource<List<Release>>> {
        return liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(barcodeLookupRepository.lookupReleasesWithBarcode("barcode:$barcode"))
        }
    }

}