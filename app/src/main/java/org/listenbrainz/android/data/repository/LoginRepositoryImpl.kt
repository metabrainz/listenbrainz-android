package org.listenbrainz.android.data.repository

import androidx.lifecycle.MutableLiveData
import org.listenbrainz.android.data.sources.api.LoginService
import org.listenbrainz.android.data.sources.api.MusicBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.entities.AccessToken
import org.listenbrainz.android.data.sources.api.entities.userdata.UserInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImpl @Inject constructor(private val service: LoginService) : LoginRepository{

    override val accessTokenLiveData: MutableLiveData<AccessToken?> = MutableLiveData()
    override val userInfoLiveData: MutableLiveData<UserInfo?> = MutableLiveData()

    override fun fetchAccessToken(code: String?) {
        service.getAccessToken(MusicBrainzServiceGenerator.AUTH_BASE_URL + "token",
                code,
                "authorization_code",
                MusicBrainzServiceGenerator.CLIENT_ID,
                MusicBrainzServiceGenerator.CLIENT_SECRET,
                MusicBrainzServiceGenerator.OAUTH_REDIRECT_URI)!!.enqueue(object : Callback<AccessToken?> {
                    override fun onResponse(call: Call<AccessToken?>, response: Response<AccessToken?>) {
                        val token = response.body()
                        accessTokenLiveData.value = token
                    }

                    override fun onFailure(call: Call<AccessToken?>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
    }

    override fun fetchUserInfo() {
        service.userInfo!!.enqueue(object : Callback<UserInfo?> {
            override fun onResponse(call: Call<UserInfo?>, response: Response<UserInfo?>) {
                val info = response.body()
                userInfoLiveData.postValue(info)
            }

            override fun onFailure(call: Call<UserInfo?>, t: Throwable) {}
        })
    }

}