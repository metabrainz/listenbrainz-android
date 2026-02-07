package org.listenbrainz.android.user

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.listenbrainz.android.BaseUnitTest
import org.listenbrainz.android.model.ResponseError
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
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUserDNE
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.ResourceString.similarUserErrorString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest : BaseUnitTest() {

    @Mock
    private lateinit var mockRepository: UserRepository

    @BeforeTest
    fun setUp() {
        // Listen Count mocks
        wheneverBlocking {
            mockRepository.fetchUserListenCount(testUsername)
        }.thenReturn(Resource.success(listenCountTestData))
        
        wheneverBlocking {
            mockRepository.fetchUserListenCount(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // Similar Users mocks
        wheneverBlocking {
            mockRepository.fetchUserSimilarity(testUsername, testSomeOtherUser)
        }.thenReturn(Resource.success(userSimilarityTestData))
        
        wheneverBlocking {
            mockRepository.fetchUserSimilarity(testUsername, testUsername)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist(actualResponse = similarUserErrorString)))

        // Current Pins mocks
        wheneverBlocking {
            mockRepository.fetchUserCurrentPins(testUsername)
        }.thenReturn(Resource.success(currentPinsTestData))
        
        wheneverBlocking {
            mockRepository.fetchUserCurrentPins(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // All Pins mocks
        wheneverBlocking {
            mockRepository.fetchUserPins(testUsername)
        }.thenReturn(Resource.success(allPinsTestData))
        
        wheneverBlocking {
            mockRepository.fetchUserPins(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // Top Artists mocks
        wheneverBlocking {
            mockRepository.getTopArtists(eq(testUsername), any(), any())
        }.thenReturn(Resource.success(topArtistsTestData))
        
        wheneverBlocking {
            mockRepository.getTopArtists(eq(testUserDNE), any(), any())
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // User Feedback mocks
        wheneverBlocking {
            mockRepository.getUserFeedback(testUsername, null)
        }.thenReturn(Resource.success(lovedHatedSongsTestData))
        
        wheneverBlocking {
            mockRepository.getUserFeedback(testUserDNE, null)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // Listening Activity mocks
        wheneverBlocking {
            mockRepository.getUserListeningActivity(eq(testUsername), any())
        }.thenReturn(Resource.success(listeningActivityTestData))
        
        wheneverBlocking {
            mockRepository.getUserListeningActivity(eq(testUserDNE), any())
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // Global Listening Activity mock
        wheneverBlocking {
            mockRepository.getGlobalListeningActivity(any())
        }.thenReturn(Resource.success(globalListeningActivityTestData))

        // Top Albums mocks
        wheneverBlocking {
            mockRepository.getTopAlbums(eq(testUsername), any(), any())
        }.thenReturn(Resource.success(topAlbumsTestData))
        
        wheneverBlocking {
            mockRepository.getTopAlbums(eq(testUserDNE), any(), any())
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))

        // Top Songs mocks
        wheneverBlocking {
            mockRepository.getTopSongs(eq(testUsername), any())
        }.thenReturn(Resource.success(topSongsTestData))
        
        wheneverBlocking {
            mockRepository.getTopSongs(eq(testUserDNE), any())
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist()))
    }

    @Test
    fun `fetch listen count for existing user`() = runTest {
        val result = mockRepository.fetchUserListenCount(testUsername)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(listenCountTestData.payload.count, result.data?.payload?.count)
    }

    @Test
    fun `fetch listen count for non-existing user`() = runTest {
        val result = mockRepository.fetchUserListenCount(testUserDNE)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch similar users for valid comparison`() = runTest {
        val result = mockRepository.fetchUserSimilarity(testUsername, testSomeOtherUser)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(userSimilarityTestData.userSimilarity?.username, result.data?.userSimilarity?.username)
    }

    @Test
    fun `fetch similar users for invalid comparison`() = runTest {
        val result = mockRepository.fetchUserSimilarity(testUsername, testUsername)

        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(similarUserErrorString, result.error?.actualResponse)
    }

    @Test
    fun `fetch current pins for existing user`() = runTest {
        val result = mockRepository.fetchUserCurrentPins(testUsername)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(currentPinsTestData?.pinnedRecording?.created, result.data?.pinnedRecording?.created)
        assertEquals(currentPinsTestData?.pinnedRecording?.blurbContent, result.data?.pinnedRecording?.blurbContent)
    }

    @Test
    fun `fetch current pins for non-existing user`() = runTest {
        val result = mockRepository.fetchUserCurrentPins(testUserDNE)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch all pins for existing user`() = runTest {
        val result = mockRepository.fetchUserPins(testUsername)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(allPinsTestData?.count, result.data?.count)
        assertEquals(allPinsTestData?.pinnedRecordings?.get(0)?.recordingMsid, result.data?.pinnedRecordings?.get(0)?.recordingMsid)
        assertEquals(allPinsTestData?.userName, result.data?.userName)
    }

    @Test
    fun `fetch all pins for non-existing user`() = runTest {
        val result = mockRepository.fetchUserPins(testUserDNE)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch top artists for existing user`() = runTest {
        val result = mockRepository.getTopArtists(testUsername, "all_time", 25)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(topArtistsTestData?.payload?.artists?.get(0)?.artistName, result.data?.payload?.artists?.get(0)?.artistName)
        assertEquals(topArtistsTestData?.payload?.count, result.data?.payload?.count)
    }

    @Test
    fun `fetch top artists for non-existing user`() = runTest {
        val result = mockRepository.getTopArtists(testUserDNE, "all_time", 25)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch loved and hated songs for existing user`() = runTest {
        val result = mockRepository.getUserFeedback(testUsername, null)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(lovedHatedSongsTestData?.count, result.data?.count)
        assertEquals(lovedHatedSongsTestData?.feedback?.get(0)?.trackMetadata?.trackName, result.data?.feedback?.get(0)?.trackMetadata?.trackName)
    }

    @Test
    fun `fetch loved and hated songs for non-existing user`() = runTest {
        val result = mockRepository.getUserFeedback(testUserDNE, null)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch user listening activity for existing user`() = runTest {
        val result = mockRepository.getUserListeningActivity(testUsername, "all_time")

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(listeningActivityTestData?.payload?.userId, result.data?.payload?.userId)
    }

    @Test
    fun `fetch user listening activity for non-existing user`() = runTest {
        val result = mockRepository.getUserListeningActivity(testUserDNE, "all_time")

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch global listening activity`() = runTest {
        val result = mockRepository.getGlobalListeningActivity("all_time")

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertNull(result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for existing user`() = runTest {
        val result = mockRepository.getTopAlbums(testUsername, "all_time", 25)

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(topAlbumsTestData?.payload?.releases?.get(0)?.releaseName, result.data?.payload?.releases?.get(0)?.releaseName)
        assertEquals(topAlbumsTestData?.payload?.userId, result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for non-existing user`() = runTest {
        val result = mockRepository.getTopAlbums(testUserDNE, "all_time", 25)

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }

    @Test
    fun `fetch top songs for existing user`() = runTest {
        val result = mockRepository.getTopSongs(testUsername, "all_time")

        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(topSongsTestData?.payload?.userId, result.data?.payload?.userId)
        assertEquals(topSongsTestData?.payload?.recordings?.get(0)?.releaseName, result.data?.payload?.recordings?.get(0)?.releaseName)
    }

    @Test
    fun `fetch top songs for non-existing user`() = runTest {
        val result = mockRepository.getTopSongs(testUserDNE, "all_time")

        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }
}
