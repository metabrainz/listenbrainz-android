package org.listenbrainz.android.repository.yim

import androidx.annotation.WorkerThread
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.service.YimService
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.Resource


class YimRepositoryImpl(
    private val service: YimService,
    private val logger: Log = Log
) : YimRepository {
    
    @WorkerThread
    override suspend fun getYimData(username: String): Resource<YimPayload> {
        return try {
            val response = service.getYimData(username = username)
            Resource(Resource.Status.SUCCESS, response)
        }catch (e: Exception){
            e.printStackTrace()
            e.localizedMessage?.let { logger.w(it) }
            Resource(Resource.Status.FAILED, null)
        }
    }
}