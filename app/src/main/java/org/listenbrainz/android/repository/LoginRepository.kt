package org.listenbrainz.android.repository

import androidx.lifecycle.MutableLiveData
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo

interface LoginRepository {

    val accessTokenLiveData: MutableLiveData<AccessToken?>
    val userInfoLiveData: MutableLiveData<UserInfo?>

    fun fetchAccessToken(code: String?)

    fun fetchUserInfo()
}