package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testYimData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testYimUsername

class MockYimRepository : YimRepository {
    
    override suspend fun getYimData(username: String): Resource<YimData> {
        return Resource(Resource.Status.SUCCESS, testYimData)
    }
    
    override fun getUsername(): String {
        return testYimUsername
    }
    
    override fun getLoginStatus(): Int {
        return LBSharedPreferences.STATUS_LOGGED_IN
    }
    
}