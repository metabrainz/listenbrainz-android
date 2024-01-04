package org.listenbrainz.android.repository.yim23

import org.listenbrainz.android.model.yimdata.Yim23Payload
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource

// TODO: TO BE REMOVED WHEN YIM GOES LIVE

interface Yim23Repository {
    suspend fun getYimData(username: String?): Resource<Yim23Payload>
}