package org.listenbrainz.android.yim

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.repository.yim.YimRepositoryImpl
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.YimRepositoryTestData.testYimData
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions
import org.listenbrainz.sharedtest.utils.EntityTestUtils.loadResourceAsString
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.RetrofitUtils

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
        val resource = repository.getYimData(testUsername)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        checkYimAssertions(expected, resource.data)
    }
    
    @After
    fun teardown() {
        webServer.close()
    }
}