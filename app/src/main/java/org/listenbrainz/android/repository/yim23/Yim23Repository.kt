package org.listenbrainz.android.repository.yim23

import org.listenbrainz.android.model.yimdata.Yim23Payload
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource

interface Yim23Repository {
    suspend fun getYimData(username: String , year : Number): Resource<Yim23Payload>
}