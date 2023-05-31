package org.listenbrainz.android.service

import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface LoginService {

    @FormUrlEncoded
    @POST
    fun getAccessToken(@Url url: String?,
                       @Field("code") code: String?,
                       @Field("grant_type") grantType: String?,
                       @Field("client_id") clientId: String?,
                       @Field("client_secret") clientSecret: String?,
                       @Field("redirect_uri") redirectUri: String?): Call<AccessToken>

    @GET("userinfo")
    fun userInfo(@Header("Authorization") token: String?): Call<UserInfo>
}