package org.listenbrainz.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.listenbrainz.android.LiveDataTestUtil.getOrAwaitValue
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.presentation.features.collection.CollectionViewModel
import org.listenbrainz.android.util.Resource

class CollectionViewModelTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCollectionDetailsViewModel() = runTest {
        val testCollectionDetails = EntityTestUtils.testCollectionDetails
        val viewModel = CollectionViewModel(MockCollectionRepository())
        launch (Dispatchers.IO) {
            val resource = getOrAwaitValue(viewModel.fetchCollectionDetails(MBEntityType.RELEASE,testCollectionDetails.mBID!!))
            Assert.assertEquals(Resource.Status.SUCCESS, resource!!.status)
            val cDIndex = resource.data!!.indexOfFirst { it.mBID == testCollectionDetails.mBID!!}
            AssertionUtils.checkCollectionDetailsAssertions(testCollectionDetails, resource.data!![cDIndex])
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCollectionViewModel() = runTest {
        val testCollection = EntityTestUtils.testCollectionPublic
        val viewModel = CollectionViewModel(MockCollectionRepository())
        launch(Dispatchers.IO) {
            val resource = getOrAwaitValue(viewModel.fetchCollectionData(testCollection.mbid!!, false))
            Assert.assertEquals(Resource.Status.SUCCESS, resource!!.status)
            val collectionPublic = resource.data!!.indexOfFirst { it.mbid == testCollection.mbid }
            AssertionUtils.checkCollectionAssertions(testCollection,
                resource.data!![collectionPublic])
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
}