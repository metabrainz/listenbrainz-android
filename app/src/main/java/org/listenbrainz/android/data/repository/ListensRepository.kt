package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.listens.Listen
import org.listenbrainz.android.util.Resource

interface ListensRepository {
    suspend fun fetchUserListens(userName: String): Resource<List<Listen>>
    suspend fun fetchCoverArt(MBID: String): Resource<CoverArt>
}