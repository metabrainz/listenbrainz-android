package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.LoginRepository.Companion.errorToken
import org.listenbrainz.android.repository.LoginRepository.Companion.errorUserInfo
import org.listenbrainz.android.service.LoginService
import org.listenbrainz.android.util.ListenBrainzServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImpl @Inject constructor(private val service: LoginService) : LoginRepository {

    override val accessTokenFlow: MutableStateFlow<AccessToken?> = MutableStateFlow(null)
    override val userInfoFlow: MutableStateFlow<UserInfo?> = MutableStateFlow(null)

    override fun fetchAccessToken(code: String?) {
        service.getAccessToken(
            ListenBrainzServiceGenerator.AUTH_BASE_URL + "token",
                code,
                "authorization_code",
                ListenBrainzServiceGenerator.CLIENT_ID,
                ListenBrainzServiceGenerator.CLIENT_SECRET,
                ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI)!!.enqueue(object : Callback<AccessToken?> {
                    override fun onResponse(call: Call<AccessToken?>, response: Response<AccessToken?>) {
                        accessTokenFlow.update { response.body() ?: errorToken }
                    }

                    override fun onFailure(call: Call<AccessToken?>, t: Throwable) {
                        t.printStackTrace()
                        accessTokenFlow.update { errorToken }
                    }
                })
    }

    override fun fetchUserInfo() {
        service.userInfo!!.enqueue(object : Callback<UserInfo?> {
            override fun onResponse(call: Call<UserInfo?>, response: Response<UserInfo?>) {
                val info = response.body()
                userInfoFlow.update { info ?: errorUserInfo }
            }

            override fun onFailure(call: Call<UserInfo?>, t: Throwable) {
                t.printStackTrace()
                userInfoFlow.update { errorUserInfo }
            }
        })
    }

}