package org.listenbrainz.android.presentation.features.userdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntity

class UserViewModel : ViewModel() {

    private val entityData = MutableLiveData<MBEntity>()

    val userData: LiveData<MBEntity> = entityData

    fun setUserData(entity: MBEntity) {
        entityData.value = entity
    }
}