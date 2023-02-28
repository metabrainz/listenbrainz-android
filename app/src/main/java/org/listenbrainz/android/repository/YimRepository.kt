package org.listenbrainz.android.repository

import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource

interface YimRepository {
    suspend fun getYimData(username: String): Resource<YimPayload>
}