package org.listenbrainz.android.yim

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.listenbrainz.android.BaseUnitTest
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.YimRepositoryTestData.testYimData
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class YimRepositoryTest : BaseUnitTest() {
    
    @Mock
    private lateinit var mockRepository: YimRepository
    
    @BeforeTest
    fun setUp() {
        // Mock YIM data response
        wheneverBlocking {
            mockRepository.getYimData(testUsername)
        }.thenReturn(Resource.success(testYimData))
    }
    
    @Test
    fun getYimData() = runTest {
        val expected = testYimData
        val resource = mockRepository.getYimData(testUsername)
        
        assertEquals(Resource.Status.SUCCESS, resource.status)
        checkYimAssertions(expected, resource.data)
    }
}
