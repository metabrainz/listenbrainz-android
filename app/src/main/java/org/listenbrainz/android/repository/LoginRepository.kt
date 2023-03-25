package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.MutableStateFlow
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo

interface LoginRepository {

    val accessTokenFlow: MutableStateFlow<AccessToken?>
    val userInfoFlow: MutableStateFlow<UserInfo?>

    fun fetchAccessToken(code: String?)

    fun fetchUserInfo()
    
    companion object {
        val errorToken: AccessToken = AccessToken()
        val errorUserInfo : UserInfo = UserInfo()
    }
}