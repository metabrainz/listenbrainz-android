package org.listenbrainz.android

import com.google.gson.Gson
import org.listenbrainz.android.EntityTestUtils.testYimUsername
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Resource

class MockYimRepository : YimRepository {
    
    override suspend fun getYimData(username: String): Resource<YimData> {
        val response = EntityTestUtils.loadResourceAsString("yim_data.json")
        val yimData = Gson().fromJson(response, YimData::class.java)
        return Resource(Resource.Status.SUCCESS, yimData)
    }
    
    override fun getUsername(): String {
        return testYimUsername
    }
    
    override fun getLoginStatus(): Int {
        return LBSharedPreferences.STATUS_LOGGED_IN
    }
    
}