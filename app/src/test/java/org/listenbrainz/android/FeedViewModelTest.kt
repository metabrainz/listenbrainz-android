package org.listenbrainz.android

import com.jasjeet.typesafe_datastore_test.MockPrimitivePreference
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFamiliarUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FeedViewModelTest: BaseUnitTest()
{
    private lateinit var viewModel : FeedViewModel
    
    @Mock
    private lateinit var socialRepository: SocialRepository
    
    @Mock
    private lateinit var feedRepository: FeedRepository
    
    @Mock
    private lateinit var appPreferences: AppPreferences
    
    @Mock
    private lateinit var listensRepository: ListensRepository
    
    @Mock
    private lateinit var remotePlaybackHandler: RemotePlaybackHandler
    
    @Before
    fun setup() {
        /*wheneverBlocking {
            feedRepository.getFeedEvents(username = testUsername, maxTs = null, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.my_feed_page_1.toClass())
        )
        
        wheneverBlocking {
            feedRepository.getFeedEvents(username = testUsername, maxTs = 1692700512, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.my_feed_page_2.toClass())
        )
        
        wheneverBlocking {
            feedRepository.getFeedSimilarListens(username = testUsername, maxTs = null, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.similar_listens_page_1.toClass())
        )
        
        wheneverBlocking {
            feedRepository.getFeedSimilarListens(username = testUsername, maxTs = 1694881863, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.similar_listens_page_2.toClass())
        )
        
        wheneverBlocking {
            feedRepository.getFeedFollowListens(username = testUsername, maxTs = null, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.follow_listens_page_1.toClass())
        )
        
        wheneverBlocking {
            feedRepository.getFeedFollowListens(username = testUsername, maxTs = 1694878358, count = FeedEventCount)
        }.doReturn(
            Resource.success(ResourceString.follow_listens_page_2.toClass())
        )*/
        
        viewModel = FeedViewModel(
            feedRepository,
            socialRepository,
            listensRepository,
            appPreferences,
            remotePlaybackHandler,
            testDispatcher(),
            testDispatcher()
        )
    }
    
    @Test
    fun `test init`() = test {
        val uiState = viewModel.uiState.value
        assertEquals(true, uiState.myFeedState.isDeletedMap.isEmpty())
        assertEquals(true, uiState.myFeedState.isHiddenMap.isEmpty())
    }
    
    @Test
    fun `test hide`() = test {
        /** Condition for hide is that the event is not user's.
         * Meanwhile for delete, user should be the one who created the event.*/
        val mockEventType = FeedEventType.RECORDING_RECOMMENDATION
        val mockEvent = FeedEvent(
            0,
            0,
            mockEventType.type,
            metadata = Metadata(username = testFamiliarUser),
            hidden = false,
            username = testFamiliarUser
        )
        wheneverBlocking {
            appPreferences.username
        }.doReturn(MockPrimitivePreference(testUsername))
        
        wheneverBlocking {
            feedRepository.hideEvent(testUsername, FeedEventVisibilityData(mockEventType.type, mockEvent.id.toString()))
        }.doReturn(Resource.success(SocialResponse()))
        
        // Calling
        viewModel.hideOrDeleteEvent(
            mockEvent,
            mockEventType,
            testUsername
        )
        
        advanceUntilIdle()
        
        val uiState = viewModel.uiState.value
        assertEquals(true, uiState.myFeedState.isHiddenMap[mockEvent.id])
    }
}