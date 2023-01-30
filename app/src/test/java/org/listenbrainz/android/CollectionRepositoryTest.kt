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
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkCollectionAssertions
import org.listenbrainz.sharedtest.utils.EntityTestUtils.loadResourceAsString
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testCollectionDetails
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testCollectionPrivate
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testCollectionPublic
import org.listenbrainz.sharedtest.utils.RetrofitUtils.createTestService
import org.listenbrainz.android.data.repository.CollectionRepository
import org.listenbrainz.android.data.repository.CollectionRepositoryImpl
import org.listenbrainz.android.data.sources.api.CollectionService
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import org.listenbrainz.sharedtest.utils.AssertionUtils

class CollectionRepositoryTest {

    private lateinit var webServer: MockWebServer
    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        webServer = MockWebServer()
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val params = request.requestUrl?.queryParameter("inc")
                val paramsDetails = request.requestUrl?.queryParameter("collection")
                val file = when {
                        params != null && params.equals("user-collections", ignoreCase = true) -> "collections_private.json"
                        paramsDetails != null -> "collection_details.json"
                        else -> "collections_public.json"
                    }
                return MockResponse().setResponseCode(200).setBody(loadResourceAsString(file))
            }
        }
        webServer.start()
        val service = createTestService(CollectionService::class.java, webServer.url("/"))
        repository = CollectionRepositoryImpl(service)
    }

    @Test
    fun fetchCollectionDetails() = runBlocking {
        val expected = testCollectionDetails
        val resource = repository.fetchCollectionDetails(MBEntityType.RELEASE.entity,expected.mBID!!)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        val collectionDetails = Utils.toResultItemsList(MBEntityType.RELEASE, resource)
        val cDIndex = collectionDetails.data!!.indexOfFirst { it.mBID == expected.mBID!!}
        AssertionUtils.checkCollectionDetailsAssertions(expected, collectionDetails.data!![cDIndex])
    }

    @Test
    fun fetchCollections() = runBlocking {
        val expectedPublic = testCollectionPublic
        val resourcePublic = repository.fetchCollections(expectedPublic.editor!!,false)
        assertEquals(Resource.Status.SUCCESS, resourcePublic.status)
        val collectionPublic = resourcePublic.data!!.indexOfFirst { it.mbid == expectedPublic.mbid }
        checkCollectionAssertions(expectedPublic, resourcePublic.data!![collectionPublic])

        val expectedPrivate = testCollectionPrivate
        val resourcePrivate = repository.fetchCollections(expectedPrivate.editor!!,true)
        assertEquals(Resource.Status.SUCCESS, resourcePrivate.status)
        val collectionPrivate = resourcePrivate.data!!.indexOfFirst { it.mbid == expectedPrivate.mbid }
        checkCollectionAssertions(expectedPrivate, resourcePrivate.data!![collectionPrivate])
    }

    @After
    fun teardown() {
        webServer.close()
    }
}