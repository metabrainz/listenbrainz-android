package org.listenbrainz.android

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.AssertionUtils.checkYimAssertions
import org.listenbrainz.android.EntityTestUtils.loadResourceAsString
import org.listenbrainz.android.EntityTestUtils.testYimData
import org.listenbrainz.android.EntityTestUtils.testYimUsername
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.repository.YimRepositoryImpl
import org.listenbrainz.android.data.sources.api.YimService
import org.listenbrainz.android.util.Resource

class YimRepositoryTest {
    
    private lateinit var webServer: MockWebServer
    private lateinit var repository: YimRepository
    
    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val file = "yim_data.json"
                return MockResponse().setResponseCode(200).setBody(loadResourceAsString(file))
            }
        }
        webServer.start()
        val service = RetrofitUtils.createTestService(YimService::class.java, webServer.url("/"))
        repository = YimRepositoryImpl(service)
    }
    
    @Test
    fun getYimData() = runBlocking {
        val expected = testYimData
        val resource = repository.getYimData(testYimUsername)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        checkYimAssertions(resource.data, expected)
    }
    
    @After
    fun teardown() {
        webServer.close()
    }
}