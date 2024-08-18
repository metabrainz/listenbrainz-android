package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.CurrentPins
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserListeningActivity
import org.listenbrainz.android.model.user.UserSimilarityPayload
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.allPinsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.currentPinsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.globalListeningActivityTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.listenCountTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.listeningActivityTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.lovedHatedSongsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.topAlbumsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.topArtistsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.topSongsTestData
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.userSimilarityTestData

class MockUserRepository : UserRepository {
    override suspend fun fetchUserListenCount(username: String?): Resource<Listens?> {
        return Resource(Resource.Status.SUCCESS, listenCountTestData)
    }

    override suspend fun fetchUserSimilarity(
        username: String?,
        otherUserName: String?
    ): Resource<UserSimilarityPayload?> {
        return Resource(Resource.Status.SUCCESS, userSimilarityTestData)
    }

    override suspend fun fetchUserCurrentPins(username: String?): Resource<CurrentPins?> {
        return Resource(Resource.Status.SUCCESS, currentPinsTestData)
    }

    override suspend fun fetchUserPins(username: String?): Resource<AllPinnedRecordings?> {
        return Resource(Resource.Status.SUCCESS, allPinsTestData)
    }

    override suspend fun getTopArtists(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopArtists> {
        return Resource(Resource.Status.SUCCESS, topArtistsTestData)
    }

    override suspend fun getUserFeedback(username: String?, score: Int?): Resource<UserFeedback?> {
        return Resource(Resource.Status.SUCCESS, lovedHatedSongsTestData)
    }

    override suspend fun getUserListeningActivity(
        username: String?,
        rangeString: String
    ): Resource<UserListeningActivity?> {
        return Resource(Resource.Status.SUCCESS, listeningActivityTestData)
    }

    override suspend fun getGlobalListeningActivity(rangeString: String): Resource<UserListeningActivity?> {
        return Resource(Resource.Status.SUCCESS, globalListeningActivityTestData)
    }

    override suspend fun getTopAlbums(
        username: String?,
        rangeString: String,
        count: Int
    ): Resource<TopAlbums> {
        return Resource(Resource.Status.SUCCESS, topAlbumsTestData)
    }

    override suspend fun getTopSongs(username: String?, rangeString: String): Resource<TopSongs> {
        return Resource(Resource.Status.SUCCESS, topSongsTestData)
    }

}