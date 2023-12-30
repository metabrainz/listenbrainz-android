package org.listenbrainz.android.repository.yim23

import androidx.annotation.WorkerThread
import org.listenbrainz.android.model.yimdata.Yim23Payload
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.service.Yim23Service
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

class Yim23RepositoryImpl @Inject constructor(private val service: Yim23Service) : Yim23Repository {

    @WorkerThread
    override suspend fun getYimData(username: String , year : Number): Resource<Yim23Payload> {
        return try {
            val response = service.getYimData(username = username , year = year)
            Resource(Resource.Status.SUCCESS, response)
        }catch (e: Exception){
            e.printStackTrace()
            e.localizedMessage?.let { Log.w(it) }
            Resource(Resource.Status.FAILED, null)
        }
    }
}