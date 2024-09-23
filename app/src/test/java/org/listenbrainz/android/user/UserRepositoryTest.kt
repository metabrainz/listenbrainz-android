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
        // Fetch listen count for a user known to exist
        val result = repository.fetchUserListenCount(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Verify that the listen count matches the expected data
        assertEquals(listenCountTestData.payload.count, result.data?.payload?.count)
    }

    @Test
    fun `fetch listen count for non-existing user`() = runTest {
        // Attempt to fetch listen count for a user that does not exist
        val result = repository.fetchUserListenCount(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch similar users for valid comparison`() = runTest {
        // Fetch similar users for a valid comparison between two different users
        val result = repository.fetchUserSimilarity(testUsername, testSomeOtherUser)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify that the similar user's username matches the expected value
        assertEquals("jivteshs20", result.data?.userSimilarity?.username)
    }

    @Test
    fun `fetch similar users for invalid comparison`() = runTest {
        // Attempt to fetch similar users for a comparison with the same user
        val result = repository.fetchUserSimilarity(testUsername, testUsername)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the error message matches the expected value for invalid comparison
        assertEquals(similarUserErrorString, result.error?.actualResponse)
    }

    @Test
    fun `fetch current pins for existing user`() = runTest {
        // Fetch current pins for a user known to exist
        val result = repository.fetchUserCurrentPins(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify that the pinned recording's creation time and blurb content match the expected values
        assertEquals(1.72335654E9f, result.data?.pinnedRecording?.created)
        assertEquals("Noice", result.data?.pinnedRecording?.blurbContent)
    }

    @Test
    fun `fetch current pins for non-existing user`() = runTest {
        // Attempt to fetch current pins for a user that does not exist
        val result = repository.fetchUserCurrentPins(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch all pins for existing user`() = runTest {
        // Fetch all pins for a user known to exist
        val result = repository.fetchUserPins(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the total count and check the first pinned recording's MSID and username
        assertEquals(12, result.data?.count)
        assertEquals("6f4a50ca-b636-4c0b-a6a0-5b84451ab014", result.data?.pinnedRecordings?.get(0)?.recordingMsid)
        assertEquals(testUsername, result.data?.userName)
    }

    @Test
    fun `fetch all pins for non-existing user`() = runTest {
        // Attempt to fetch all pins for a user that does not exist
        val result = repository.fetchUserPins(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch top artists for existing user`() = runTest {
        // Fetch top artists for a user known to exist
        val result = repository.getTopArtists(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify that the first artist's name and the total count of top artists match the expected values
        assertEquals("Karan Aujla", result.data?.payload?.artists?.get(0)?.artistName)
        assertEquals(25, result.data?.payload?.count)
    }

    @Test
    fun `fetch top artists for non-existing user`() = runTest {
        // Attempt to fetch top artists for a user that does not exist
        val result = repository.getTopArtists(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch loved and hated songs for existing user`() = runTest {
        // Fetch loved and hated songs for a user known to exist
        val result = repository.getUserFeedback(testUsername, null)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the count of feedback and the name of the first loved song
        assertEquals(25, result.data?.count)
        assertEquals("Calling", result.data?.feedback?.get(0)?.trackMetadata?.trackName)

        // Verify that the feedback user ID matches the expected username
        assertEquals(testUsername, result?.data?.feedback?.get(2)?.userId)
    }

    @Test
    fun `fetch loved and hated songs for non-existing user`() = runTest {
        // Attempt to fetch loved and hated songs for a user that does not exist
        val result = repository.getUserFeedback(testUserDNE, null)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch user listening activity for existing user`() = runTest {
        // Fetch user listening activity for a user known to exist
        val result = repository.getUserListeningActivity(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the user ID and listen count for a specific activity
        assertEquals(testUsername, result.data?.payload?.userId)
        assertEquals(2826, result.data?.payload?.listeningActivity?.get(21)?.listenCount)
    }

    @Test
    fun `fetch user listening activity for non-existing user`() = runTest {
        // Attempt to fetch user listening activity for a user that does not exist
        val result = repository.getUserListeningActivity(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch global listening activity`() = runTest {
        // Fetch global listening activity, which is not user-specific
        val result = repository.getGlobalListeningActivity()

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the listen count for a specific activity and ensure userId is null for global data
        assertEquals(4499100, result.data?.payload?.listeningActivity?.get(3)?.listenCount)
        assertNull(result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for existing user`() = runTest {
        // Fetch top albums for a user known to exist
        val result = repository.getTopAlbums(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the release name of the third album and the username associated with the data
        assertEquals("Small Circle", result.data?.payload?.releases?.get(2)?.releaseName)
        assertEquals(testUsername, result.data?.payload?.userId)
    }

    @Test
    fun `fetch top albums for non-existing user`() = runTest {
        // Attempt to fetch top albums for a user that does not exist
        val result = repository.getTopAlbums(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }

    @Test
    fun `fetch top songs for existing user`() = runTest {
        // Fetch top songs for a user known to exist
        val result = repository.getTopSongs(testUsername)

        // Assert that the operation was successful
        assertEquals(Resource.Status.SUCCESS, result.status)

        // Ensure that the result data is not null
        assertNotNull(result.data)

        // Verify the user ID and the name of the top song
        assertEquals(testUsername, result.data?.payload?.userId)
        assertEquals("Small Circle", result.data?.payload?.recordings?.get(0)?.releaseName)
    }

    @Test
    fun `fetch top songs for non-existing user`() = runTest {
        // Attempt to fetch top songs for a user that does not exist
        val result = repository.getTopSongs(testUserDNE)

        // Assert that the operation failed
        assertEquals(Resource.Status.FAILED, result.status)

        // Verify that the correct error is returned for a non-existent user
        assertEquals(ResponseError.DOES_NOT_EXIST, result.error)
    }


}