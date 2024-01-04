package org.listenbrainz.android.repository.yim23

import androidx.annotation.WorkerThread
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.yimdata.Yim23Payload
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.service.Yim23Service
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import retrofit2.http.GET
import javax.inject.Inject

// TODO: TO BE REMOVED WHEN YIM GOES LIVE

class Yim23RepositoryImpl @Inject constructor(private val service: Yim23Service) : Yim23Repository {


    override suspend fun getYimData(username: String?): Resource<Yim23Payload> {
        return Utils.parseResponse {
            if(username == null) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
            service.getYimData(username)
        }
    }
}