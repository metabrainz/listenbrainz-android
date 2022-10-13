package org.listenbrainz.android.presentation.features.base

import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.data.repository.LookupRepository
import org.listenbrainz.android.data.sources.Constants
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntity
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.SUCCESS

abstract class LookupViewModel<T : MBEntity> protected constructor(val repository: LookupRepository, val entity: MBEntityType) : ViewModel() {

    val mbid: MutableLiveData<String> = MutableLiveData()

    protected val jsonLiveData: LiveData<Resource<String>> = mbid.switchMap {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            if (it == null) {
                emit(Resource.failure())
            }
            else{
                emit(repository.fetchData(entity.entity, it, Constants.getDefaultParams(entity)))
            }
        }
    }

    abstract val data: LiveData<out Resource<T>>

    inline fun <reified T> parseData(data: Resource<String>): Resource<T> {
        return try {
            if (data.status == SUCCESS) {
                Resource(SUCCESS, Gson().fromJson(data.data, T::class.java))
            }
            else {
                Resource.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.failure()
        }
    }

}