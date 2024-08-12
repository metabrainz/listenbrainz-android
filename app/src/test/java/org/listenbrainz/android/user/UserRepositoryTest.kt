package org.listenbrainz.android.user

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.repository.user.UserRepositoryImpl
import org.listenbrainz.android.service.UserService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.UserRepositoryTestData.listenCountTestData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUserDNE
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.ResourceString.all_pins
import org.listenbrainz.sharedtest.utils.ResourceString.current_pins
import org.listenbrainz.sharedtest.utils.ResourceString.globalListeningActivity
import org.listenbrainz.sharedtest.utils.ResourceString.listenCount
import org.listenbrainz.sharedtest.utils.ResourceString.loved_hated_songs
import org.listenbrainz.sharedtest.utils.ResourceString.similarUser
import org.listenbrainz.sharedtest.utils.ResourceString.similarUserError
import org.listenbrainz.sharedtest.utils.ResourceString.similarUserErrorString
import org.listenbrainz.sharedtest.utils.ResourceString.topAlbums
import org.listenbrainz.sharedtest.utils.ResourceString.topSongs
import org.listenbrainz.sharedtest.utils.ResourceString.top_artists
import org.listenbrainz.sharedtest.utils.ResourceString.userListeningActivity
import org.listenbrainz.sharedtest.utils.ResourceString.user_does_not_exist_error
import org.listenbrainz.sharedtest.utils.RetrofitUtils

class UserRepositoryTest {

    private lateinit var webServer: MockWebServer
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.dispatcher = object: Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.path){
                    // Listen Count
                    "/user/$testUsername/listen-count" -> {
                        MockResponse().setResponseCode(200).setBody(
                            listenCount
                        )
                    }

                    "/user/$testUserDNE/listen-count" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Similar Users
                    "/user/$testUsername/similar-to/$testSomeOtherUser" -> {
                        MockResponse().setResponseCode(200).setBody(
                            similarUser
                        )
                    }

                    "/user/$testUsername/similar-to/$testUsername" -> {
                        MockResponse().setResponseCode(404).setBody(
                            similarUserError
                        )
                    }

                    // Current pins
                    "/$testUsername/pins/current" -> {
                        MockResponse().setResponseCode(200).setBody(
                            current_pins
                        )
                    }

                    "/$testUserDNE/pins/current" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // All Pins
                    "/$testUsername/pins" -> {
                        MockResponse().setResponseCode(200).setBody(
                            all_pins
                        )
                    }

                    "/$testUserDNE/pins" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Artists
                    "/stats/user/$testUsername/artists?range=all_time&count=25" -> {
                        MockResponse().setResponseCode(200).setBody(
                            top_artists
                        )
                    }

                    "/stats/user/$testUserDNE/artists?range=all_time&count=25" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Loved Hated Songs
                    "/feedback/user/$testUsername/get-feedback?metadata=true" -> {
                        MockResponse().setResponseCode(200).setBody(
                            loved_hated_songs
                        )
                    }

                    "/feedback/user/$testUserDNE/get-feedback?metadata=true" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Listening Activity
                    "/stats/user/$testUsername/listening-activity?range=all_time" -> {
                        MockResponse().setResponseCode(200).setBody(
                            userListeningActivity
                        )
                    }

                    "/stats/user/$testUserDNE/listening-activity?range=all_time" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Global Listening Activity
                    "/stats/sitewide/listening-activity?range=all_time" -> {
                        MockResponse().setResponseCode(200).setBody(
                            globalListeningActivity
                        )
                    }

                    // Top Albums
                    "/stats/user/$testUsername/releases?range=all_time" -> {
                        MockResponse().setResponseCode(200).setBody(
                            topAlbums
                        )
                    }

                    "/stats/user/$testUserDNE/releases?range=all_time" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    // Top Songs
                    "/stats/user/$testUsername/recordings?range=all_time" -> {
                        MockResponse().setResponseCode(200).setBody(
                            topSongs
                        )
                    }

                    "/stats/user/$testUserDNE/recordings?range=all_time" -> {
                        MockResponse().setResponseCode(404).setBody(
                            user_does_not_exist_error
                        )
                    }

                    else -> {
                        MockResponse().setResponseCode(404)
                    }
                }
            }

        }
        webServer.start()
        val service = RetrofitUtils.createTestService(UserService::class.java, webServer.url("/"))
        repository = UserRepositoryImpl(service)
    }

    @After
    fun teardown() {
        webServer.close()
    }

    @Test
    fun `fetch listen count for existing user`() = runTest {
        val result = repository.fetchUserListenCount(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(listenCountTestData.payload.count, result.data?.payload?.count)
    }

    @Test
    fun `fetch listen count for non-existing user`() = runTest {
        val result = repository.fetchUserListenCount(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch similar users for valid comparison`() = runTest {
        val result = repository.fetchUserSimilarity(testUsername, testSomeOtherUser)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals("jivteshs20", result.data?.userSimilarity?.username)
    }

    @Test
    fun `fetch similar users for invalid comparison`() = runTest {
        val result = repository.fetchUserSimilarity(testUsername, testUsername)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(similarUserErrorString, result.error?.actualResponse)
    }

    @Test
    fun `fetch current pins for existing user`() = runTest {
        val result = repository.fetchUserCurrentPins(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(1.72335654E9f, result.data?.pinnedRecording?.created)
        assertEquals("Noice", result.data?.pinnedRecording?.blurbContent)
    }

    @Test
    fun `fetch current pins for non-existing user`() = runTest {
        val result = repository.fetchUserCurrentPins(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch all pins for existing user`() = runTest {
        val result = repository.fetchUserPins(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(12, result.data?.count)
        assertEquals("6f4a50ca-b636-4c0b-a6a0-5b84451ab014", result.data?.pinnedRecordings?.get(0)?.recordingMsid)
        assertEquals(testUsername, result.data?.userName)
    }

    @Test
    fun `fetch all pins for non-existing user`() = runTest {
        val result = repository.fetchUserPins(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch top artists for existing user`() = runTest {
        val result = repository.getTopArtists(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals("Karan Aujla", result.data?.payload?.artists?.get(0)?.artistName)
        assertEquals(25, result.data?.payload?.count)
    }

    @Test
    fun `fetch top artists for non-existing user`() = runTest {
        val result = repository.getTopArtists(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch loved and hated songs for existing user`() = runTest {
        val result = repository.getUserFeedback(testUsername, null)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(25, result.data?.count)
        assertEquals("Calling", result.data?.feedback?.get(0)?.trackMetadata?.trackName)
        assertEquals(testUsername, result?.data?.feedback?.get(2)?.userId)
    }

    @Test
    fun `fetch loved and hated songs for non-existing user`() = runTest {
        val result = repository.getUserFeedback(testUserDNE, null)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch user listening activity for existing user`() = runTest {
        val result = repository.getUserListeningActivity(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(testUsername, result.data?.payload?.userId)
        assertEquals(2826, result.data?.payload?.listeningActivity?.get(21)?.listenCount)
    }

    @Test
    fun `fetch user listening activity for non-existing user`() = runTest {
        val result = repository.getUserListeningActivity(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch global listening activity`() = runTest {
        val result = repository.getGlobalListeningActivity()
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(4499100, result.data?.payload?.listeningActivity?.get(3)?.listenCount)
        assertNull(result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for existing user`() = runTest {
        val result = repository.getTopAlbums(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals("Small Circle", result.data?.payload?.releases?.get(2)?.releaseName)
        assertEquals(testUsername, result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for non-existing user`() = runTest {
        val result = repository.getTopAlbums(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch top songs for existing user`() = runTest {
        val result = repository.getTopSongs(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNotNull(result.data)
        assertEquals(testUsername, result.data?.payload?.userId)
        assertEquals("Small Circle", result.data?.payload?.recordings?.get(0)?.releaseName)
    }

    @Test
    fun `fetch top songs for non-existing user`() = runTest {
        val result = repository.getTopSongs(testUserDNE)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

}