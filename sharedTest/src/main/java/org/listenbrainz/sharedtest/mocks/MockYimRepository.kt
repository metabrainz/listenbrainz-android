package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.YimRepositoryTestData.testYimData

class MockYimRepository : YimRepository {

    override suspend fun getYimData(username: String): Resource<YimPayload> {
        return Resource(Resource.Status.SUCCESS, testYimData)
    }

}
