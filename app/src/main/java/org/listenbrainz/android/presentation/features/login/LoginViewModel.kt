package org.listenbrainz.android.presentation.features.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.listenbrainz.android.data.repository.LoginRepository
import org.listenbrainz.android.data.sources.api.entities.AccessToken
import org.listenbrainz.android.data.sources.api.entities.userdata.UserInfo
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private var repository: LoginRepository?) : ViewModel() {
    var accessTokenLiveData: MutableLiveData<AccessToken?>? = null
        get() {
            if (field == null) field = repository!!.accessTokenLiveData
            return field
        }
        private set
    var userInfoLiveData: MutableLiveData<UserInfo?>? = null
        get() {
            if (field == null) field = repository!!.userInfoLiveData
            return field
        }
        private set

    fun fetchAccessToken(code: String?) {
        repository!!.fetchAccessToken(code)
    }

    fun fetchUserInfo() {
        repository!!.fetchUserInfo()
    }

    override fun onCleared() {
        super.onCleared()
        repository = null
    }
}