package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

class AccessToken {
    @SerializedName("access_token")
    var accessToken: String? = null

    @SerializedName("expires_in")
    var expiresIn: Long = 0

    @SerializedName("refresh_token")
    var refreshToken: String? = null

    @SerializedName("token_type")
    var tokenType: String? = null
}