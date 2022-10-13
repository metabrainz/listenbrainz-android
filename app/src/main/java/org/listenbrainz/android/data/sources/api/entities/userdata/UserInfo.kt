package org.listenbrainz.android.data.sources.api.entities.userdata

import com.google.gson.annotations.SerializedName

class UserInfo {
    @SerializedName("metabrainz_user_id")
    var userId: String? = null
    var profile: String? = null

    @SerializedName("sub")
    var username: String? = null
}