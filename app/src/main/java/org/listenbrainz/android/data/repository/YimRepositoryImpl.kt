package org.listenbrainz.android.data.repository

import androidx.annotation.WorkerThread
import org.listenbrainz.android.data.sources.api.YimService
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

class YimRepositoryImpl @Inject constructor(private val service: YimService) : YimRepository {
    
    @WorkerThread
    override suspend fun getYimData(username: String): Resource<YimData> {
        return try {
            val response = service.getYimData(username = username)
            Resource(Resource.Status.SUCCESS, response)
        }catch (e: Exception){
            e.printStackTrace()
            Resource(Resource.Status.FAILED, null)
        }
    }
    
    override fun getUsername(): String? {
        return LBSharedPreferences.username
    }
    
    override fun getLoginStatus(): Int {
        return LBSharedPreferences.loginStatus
    }
}