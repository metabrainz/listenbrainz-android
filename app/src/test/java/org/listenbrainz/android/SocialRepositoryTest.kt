package org.listenbrainz.android

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.repository.SocialRepositoryImpl
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.utils.AssertionUtils
import org.listenbrainz.sharedtest.utils.EntityTestUtils
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAuthHeader
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testErrorUsername
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.RetrofitUtils

class SocialRepositoryTest {
    
    private lateinit var webServer: MockWebServer
    private lateinit var repository: SocialRepository
    
    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    "user/${testUsername}/followers" -> return MockResponse().setResponseCode(200).setBody(
                        EntityTestUtils.loadResourceAsString(
                            "followers_response.json"
                        )
                    )
    
                    "user/${testErrorUsername}/followers" -> return MockResponse().setResponseCode(404).setBody(
                        EntityTestUtils.loadResourceAsString(
                            "following_follower_error_response.json"
                        )
                    )
                    
                    "user/${testErrorUsername}/following" -> return MockResponse().setResponseCode(404).setBody(
                        EntityTestUtils.loadResourceAsString(
                            "following_follower_error_response.json"
                        )
                    )
                    
                    "user/${testUsername}/following" -> return MockResponse().setResponseCode(200).setBody(
                        EntityTestUtils.loadResourceAsString(
                            "following_response.json"
                        )
                    )
                    
                    "user/${testUsername}/follow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader){
                            setResponseCode(201).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "status_ok.json"
                                )
                            )
                        }else {
                            setResponseCode(401).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "auth_header_not_found_error.json"
                                )
                            )
                        }
                    }
                    
                    "user/${testSomeOtherUser}/unfollow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader) {
                            setResponseCode(201).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "status_ok.json"
                                )
                            )
                        } else {
                            setResponseCode(401).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "auth_header_not_found_error.json"
                                )
                            )
                        }
                    }
                    
                    "user/${testUsername}/unfollow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader) {
                            setResponseCode(400).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "cannot_follow_self_error.json"
                                )
                            )
                        } else {
                            setResponseCode(401).setBody(
                                EntityTestUtils.loadResourceAsString(
                                    "auth_header_not_found_error.json"
                                )
                            )
                        }
                    }
    
                    "user/$testUsername/similar-users" -> return MockResponse().setResponseCode(200).setBody(
                        EntityTestUtils.loadResourceAsString(
                            "similar_users_response.json"
                        )
                    )
                    
                    "search/users&search_term=$testUsername" -> {
                        MockResponse().setResponseCode(200).setBody(
                            EntityTestUtils.loadResourceAsString(
                                "search_response.json"
                            )
                        )
                    }
                }
                
                return MockResponse().setResponseCode(400)
                
            }
        }
        webServer.start()
        val service = RetrofitUtils.createTestService(SocialService::class.java, webServer.url("/"))
        repository = SocialRepositoryImpl(service)
    }
    
    @After
    fun teardown() {
        webServer.close()
    }
    
    @Test
    fun `test following success response`() = runTest {
        webServer.enqueue(MockResponse().setResponseCode(200).setBody(
            EntityTestUtils.loadResourceAsString(
                "following_response.json"
            ))
        )
        
        val expected = EntityTestUtils.testYimData
        val resource = repository.getFollowers(testUsername)
        Assert.assertEquals(Resource.Status.SUCCESS, resource.status)
        AssertionUtils.checkFollowingAssertions()
    }
    
    
    
}