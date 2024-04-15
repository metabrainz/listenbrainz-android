package org.listenbrainz.android

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.feed.FeedRepositoryImpl
import org.listenbrainz.android.service.FeedService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.ResourceString.follow_listens_page_1
import org.listenbrainz.sharedtest.utils.ResourceString.my_feed_page_1
import org.listenbrainz.sharedtest.utils.ResourceString.similar_listens_page_1
import org.listenbrainz.sharedtest.utils.ResourceString.status_ok
import org.listenbrainz.sharedtest.utils.RetrofitUtils
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FeedRepositoryTest : BaseUnitTest() {
    
    private lateinit var webServer: MockWebServer
    private lateinit var repository: FeedRepository
    @Before
    fun setup() {
        webServer = MockWebServer()
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
    
                    "/user/$testUsername/feed/events?count=${FeedRepository.FeedEventCount}" -> MockResponse().setResponseCode(200).setBody(
                        my_feed_page_1
                    )
                    
                    "/user/$testUsername/feed/events/listens/following?count=${FeedRepository.FeedListensCount}" -> MockResponse().setResponseCode(200).setBody(
                        follow_listens_page_1
                    )
                    
                    "/user/$testUsername/feed/events/listens/similar?count=${FeedRepository.FeedListensCount}" -> MockResponse().setResponseCode(200).setBody(
                        similar_listens_page_1
                    )
    
                    "/user/$testUsername/feed/events/delete" -> MockResponse().setResponseCode(200).setBody(
                        status_ok
                    )
                    
                    "/user/$testUsername/feed/events/hide" -> MockResponse().setResponseCode(200).setBody(
                        status_ok
                    )
                    
                    "/user/$testUsername/feed/events/unhide" -> MockResponse().setResponseCode(200).setBody(
                        status_ok
                    )
                    
                    else -> MockResponse().setResponseCode(400)
                }
            
            }
        }
        webServer.start()
        val service = RetrofitUtils.createTestService(FeedService::class.java, webServer.url("/"))
        repository = FeedRepositoryImpl(service)
    }

    @After
    fun teardown() {
        webServer.close()
    }
    
    @Test
    fun `test getFeedEvents`() = test {
        val result = repository.getFeedEvents(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(my_feed_page_1, TypeToken.get(FeedData::class.java)), result.data)
    }
    
    @Test
    fun `test getFeedFollowListens`() = test {
        val result = repository.getFeedFollowListens(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(follow_listens_page_1, TypeToken.get(FeedData::class.java)), result.data)
    }
    
    @Test
    fun `getFeedSimilarListens - success`() = test {
        val result = repository.getFeedSimilarListens(testUsername)
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(similar_listens_page_1, TypeToken.get(FeedData::class.java)), result.data)
    }
    
    @Test
    fun `deleteEvent - success`() = test {
        val result = repository.deleteEvent(testUsername, FeedEventDeletionData())
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(status_ok, TypeToken.get(SocialResponse::class.java)), result.data)
    }
    
    @Test
    fun `hideEvent - success`() = test {
        val result = repository.hideEvent(testUsername, FeedEventVisibilityData())
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(status_ok, TypeToken.get(SocialResponse::class.java)), result.data)
    }
    
    @Test
    fun `unhideEvent - success`() = test {
        val result = repository.hideEvent(testUsername, FeedEventVisibilityData())
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(Gson().fromJson(status_ok, TypeToken.get(SocialResponse::class.java)), result.data)
    }
    
}