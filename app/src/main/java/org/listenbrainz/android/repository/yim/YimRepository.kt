package org.listenbrainz.android.repository.yim

import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource

interface YimRepository {
    suspend fun getYimData(username: String): Resource<YimPayload>
}