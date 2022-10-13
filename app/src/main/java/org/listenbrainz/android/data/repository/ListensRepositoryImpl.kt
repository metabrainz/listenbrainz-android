package org.listenbrainz.android.data.repository

import androidx.annotation.WorkerThread
import org.listenbrainz.android.data.sources.api.ListensService
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.listens.Listen
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListensRepositoryImpl @Inject constructor(val service: ListensService) : ListensRepository{

    @WorkerThread
    override suspend fun fetchUserListens(userName: String): Resource<List<Listen>> {
        return try {
            val response = service.getUserListens(user_name = userName)
            Resource(SUCCESS, response.payload.listens)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(FAILED, null)
        }
    }

    override suspend fun fetchCoverArt(MBID: String): Resource<CoverArt> {
        return try {
            val coverArt = service.getCoverArt(MBID)
            Resource(SUCCESS, coverArt)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.failure()
        }
    }
}