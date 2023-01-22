package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.util.Resource

interface YimRepository {
    suspend fun getYimData(username: String): Resource<YimData>
    
    fun getUsername(): String?
    
    fun getLoginStatus(): Int
}