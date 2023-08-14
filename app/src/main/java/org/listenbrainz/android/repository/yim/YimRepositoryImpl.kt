package org.listenbrainz.android.repository.yim

import androidx.annotation.WorkerThread
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

class YimRepositoryImpl @Inject constructor(private val service: YimService) : YimRepository {
    
    @WorkerThread
    override suspend fun getYimData(username: String): Resource<YimPayload> {
        return try {
            val response = service.getYimData(username = username)
            Resource(Resource.Status.SUCCESS, response)
        }catch (e: Exception){
            e.printStackTrace()
            e.localizedMessage?.let { Log.w(it) }
            Resource(Resource.Status.FAILED, null)
        }
    }
}