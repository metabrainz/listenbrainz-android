package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Composable
import org.listenbrainz.android.ui.screens.feed.events.FollowFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ListenFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ListenLikeFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.NotificationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.PersonalRecommendationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.PinFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.RecordingRecommendationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ReviewFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.UnknownFeedLayout
import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.model.feed.FeedEventType
import org.listenbrainz.shared.model.feed.FeedEventType.FOLLOW
import org.listenbrainz.shared.model.feed.FeedEventType.LIKE
import org.listenbrainz.shared.model.feed.FeedEventType.LISTEN
import org.listenbrainz.shared.model.feed.FeedEventType.NOTIFICATION
import org.listenbrainz.shared.model.feed.FeedEventType.PERSONAL_RECORDING_RECOMMENDATION
import org.listenbrainz.shared.model.feed.FeedEventType.RECORDING_PIN
import org.listenbrainz.shared.model.feed.FeedEventType.RECORDING_RECOMMENDATION
import org.listenbrainz.shared.model.feed.FeedEventType.REVIEW
import org.listenbrainz.shared.model.feed.FeedEventType.UNKNOWN


/**
 * @param parentUser This is used to display pronouns of the user in a feed event. If the event is of
 * the logged in users, "You" is displayed, else normal name is displayed.*/
@Composable
fun FeedEventContent(
    type: FeedEventType,
    event: FeedEvent,
    parentUser: String,
    isHidden: Boolean,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    dropDownState: Int?,
    index: Int,
    onOpenInMusicBrainz: () -> Unit,
    onRecommend: () -> Unit,
    onPersonallyRecommend: () -> Unit,
    onReview: () -> Unit,
    onPin: () -> Unit,
    onClick: () -> Unit,
    goToUserPage: (String) -> Unit,
    goToArtistPage: (String) -> Unit,
){
    when (type){
        RECORDING_RECOMMENDATION -> RecordingRecommendationFeedLayout(
            event = event,
            parentUser = parentUser,
            isHidden = isHidden,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        PERSONAL_RECORDING_RECOMMENDATION -> PersonalRecommendationFeedLayout(
            event = event,
            parentUser = parentUser,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        RECORDING_PIN -> PinFeedLayout(
            event = event,
            isHidden = isHidden,
            parentUser = parentUser,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        LIKE -> ListenLikeFeedLayout(
            event = event,
            parentUser = parentUser,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        LISTEN -> ListenFeedLayout(
            event = event,
            parentUser = parentUser,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        FOLLOW -> FollowFeedLayout(event = event, parentUser = parentUser, goToUserPage = goToUserPage)
        NOTIFICATION -> NotificationFeedLayout(event = event, onDeleteOrHide = onDeleteOrHide, goToUserPage = goToUserPage)
        REVIEW -> ReviewFeedLayout(
            event = event,
            parentUser = parentUser,
            onDeleteOrHide = onDeleteOrHide,
            onDropdownClick = onDropdownClick,
            onClick = onClick,
            dropdownState = dropDownState,
            index = index,
            onOpenInMusicBrainz = onOpenInMusicBrainz,
            onPin = onPin,
            onReview = onReview,
            onPersonallyRecommend = onPersonallyRecommend,
            onRecommend = onRecommend,
            goToUserPage = goToUserPage,
            goToArtistPage = goToArtistPage
        )
        UNKNOWN -> UnknownFeedLayout(event = event)
    }
}