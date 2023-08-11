package org.listenbrainz.android.model

import android.content.ActivityNotFoundException
import androidx.annotation.DrawableRes
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.feed.events.FollowFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ListenFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ListenLikeFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.NotificationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.PersonalRecommendationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.PinFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.RecordingRecommendationFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.ReviewFeedLayout
import org.listenbrainz.android.ui.screens.feed.events.UnknownFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w
import org.listenbrainz.android.util.TypeConverter
import org.listenbrainz.android.util.Utils.getArticle

/**
 * @param icon Feed icon for the event, **must** be of width 19 dp.
 * @param isDeletable Can only delete our (user's) recommendations and pins.
 * @param isHideable Can only hide followed user's events and notifications.*/
enum class FeedEventType (
    val type: String,
    @DrawableRes val icon: Int,
    val isPlayable: Boolean = true,
    val isDeletable: Boolean = false,
    val isHideable: Boolean = false,
) {
    
    RECORDING_RECOMMENDATION(
        type = "recording_recommendation",
        icon = R.drawable.feed_send,
        isDeletable = true,
        isHideable = true,
    ),
    
    PERSONAL_RECORDING_RECOMMENDATION(
        type = "personal_recording_recommendation",
        icon = R.drawable.feed_send,
    ),
    
    RECORDING_PIN(
        type = "recording_pin",
        icon = R.drawable.feed_pin,
        isDeletable = true,
        isHideable = true,
    ),
    
    LIKE(
        type = "like",
        icon = R.drawable.feed_love,
    ),
    
    LISTEN(
        type = "listen",
        icon = R.drawable.feed_listen,
    ),
    
    FOLLOW(
        type = "follow",
        icon = R.drawable.feed_follow,
        isPlayable = false,
    ),
    
    NOTIFICATION(
        type = "notification",
        icon = R.drawable.feed_notification,
        isPlayable = false,
        isDeletable = true,
    ),
    
    REVIEW (
        type = "critiquebrainz_review",
        icon = R.drawable.feed_review,
    ),
    
    /** In case a new event is added in future that had not been published to the app. */
    UNKNOWN(
        type = "update_app",
        icon = R.drawable.feed_unknown,
        isPlayable = false,
    );
    
    /**
     * @param parentUser This is used to display pronouns of the user in a feed event. If the event is of
     * the logged in users, "You" is displayed, else normal name is displayed.*/
    @Composable
    fun Content(
        event: FeedEvent,
        parentUser: String,
        isHidden: Boolean,
        onDeleteOrHide: () -> Unit,
        onDropdownClick: () -> Unit,
        onClick: () -> Unit
    ){
        when (this){
            RECORDING_RECOMMENDATION -> RecordingRecommendationFeedLayout(event, parentUser, isHidden, onDeleteOrHide, onDropdownClick, onClick)
            PERSONAL_RECORDING_RECOMMENDATION -> PersonalRecommendationFeedLayout(event, parentUser, onDeleteOrHide, onDropdownClick, onClick)
            RECORDING_PIN -> PinFeedLayout(event, isHidden, parentUser, onDeleteOrHide, onDropdownClick, onClick)
            LIKE -> ListenLikeFeedLayout(event, parentUser, onDeleteOrHide, onDropdownClick, onClick)
            LISTEN -> ListenFeedLayout(event, parentUser, onDeleteOrHide, onDropdownClick, onClick)
            FOLLOW -> FollowFeedLayout(event, parentUser)
            NOTIFICATION -> NotificationFeedLayout(event, onDeleteOrHide)
            REVIEW -> ReviewFeedLayout(event, parentUser, onDeleteOrHide, onDropdownClick, onClick)
            UNKNOWN -> UnknownFeedLayout(event)
        }
    }
    
    @Composable
    fun Tagline(
        modifier: Modifier = Modifier,
        event: FeedEvent,
        parentUser: String
    ) {
        val linkStyle = SpanStyle(
            color = ListenBrainzTheme.colorScheme.lbSignature,
            fontWeight = FontWeight.Bold
        )
    
        val normalStyle = SpanStyle(
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Light
        )
    
        val uriHandler = LocalUriHandler.current
        
        val (annotatedString, onClick) = remember {
            getAnnotatedString(event, parentUser, normalStyle, linkStyle, uriHandler)
        }
        
        // Our main tagline composable
        ClickableText(
            modifier = modifier,
            text = annotatedString,
        ) { charOffset ->
            onClick(charOffset)
        }
        
        
    }
    
    
    private fun getAnnotatedString(
        event: FeedEvent,
        parentUser: String,
        normalStyle: SpanStyle,
        linkStyle: SpanStyle,
        uriHandler: UriHandler
    ): Pair<AnnotatedString, (Int) -> Unit> {
        return when (this) {
            NOTIFICATION -> {
                val annotatedLinkString = buildAnnotatedString {
                
                    withStyle(normalStyle){
                        append("Your daily-jams playlist has been updated. ")
                    }
                
                    withStyle(linkStyle){
                        append("Give it a listen!")
                    }
                
                    val str = "Your daily-jams playlist has been updated. Give it a listen!"
                    //"Your daily-jams playlist has been updated. <a href=\"link\">Give it a listen!</a>."
                
                    event.metadata.message?.let {
                        addStringAnnotation(
                            tag = "link",
                            start = str.indexOf("Give"),
                            end = str.lastIndex,
                            annotation = it.substring(52..(it.lastIndex - 24))
                        )
                    }
                }
                
                // Result
                Pair(
                    annotatedLinkString
                ) { charOffset ->
                    try {
                        annotatedLinkString
                            .getStringAnnotations(charOffset, charOffset)
                            .firstOrNull()?.let { stringAnnotation ->
                                d(stringAnnotation.item)
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    } catch (e: ActivityNotFoundException) {
                        w("MyFeed: Notification link invalid.")
                        e.printStackTrace()
                    }
                    
                }
                
            }
        
            UNKNOWN -> {
                Pair(
                    buildAnnotatedString {
                        withStyle(normalStyle){
                            append("Oops! Looks like you need to update your app.")
                        }
                    }
                ) {}
            }
        
            else -> {
                Pair(
                    constructTagline( event, parentUser, normalStyle, linkStyle)
                ) { charOffset ->
                    // TODO: Navigate to user's profile with `stringAnnotation.item`
                    //  "user1" for firstUsername and "user2" for second username.
                }
            }
        }
    }
    
    private fun constructTagline(
        feedEvent: FeedEvent,
        parentUser: String,
        normalStyle: SpanStyle,
        linkStyle: SpanStyle
    ): AnnotatedString {
        
        val emptyString = buildAnnotatedString {}
        
        // Checking if first input is present or not.
        if (feedEvent.username == null) return emptyString
        
        val firstUsername = if(isUserSelf(feedEvent, parentUser)) "You" else feedEvent.username
        
        val firstAnnotatedString = buildAnnotatedString {
            withStyle(style = linkStyle){
                append("$firstUsername ")
            }
            
            addStringAnnotation(
                tag = "user1",
                annotation = feedEvent.username,
                start = 0,
                end = firstUsername.lastIndex
            )
        }
        
        val secondAnnotatedString = when (this) {
            RECORDING_RECOMMENDATION -> {
                buildAnnotatedString {
                    withStyle(normalStyle){
                        append("recommended a track.")
                    }
                }
            }
            PERSONAL_RECORDING_RECOMMENDATION -> {
                buildAnnotatedString {
                    withStyle(normalStyle){
                        append("personally recommended a track.")
                    }
                }
            }
            RECORDING_PIN -> {
                buildAnnotatedString {
                    withStyle(normalStyle){
                        append("pinned a track.")
                    }
                }
            }
            LIKE -> {
                buildAnnotatedString {
                    withStyle(normalStyle){
                        append("liked a track.")
                    }
                }
            }
            LISTEN -> {
                buildAnnotatedString {
                    withStyle(normalStyle){
                        append("listened to a track.")
                    }
                }
            }
            FOLLOW -> {
                buildAnnotatedString {
                    
                    /** Note: For follow events, user0 and username are always going to be same.*/
                    
                    // Checking if second is present or not
                    val secondUsername = feedEvent.metadata.user1 ?: return emptyString
                    
                    val str = "$firstUsername is now following $secondUsername."
                    withStyle(normalStyle){
                        append("${if (isUserSelf(feedEvent, parentUser)) "are" else "is"} now following")
                    }
                    withStyle(linkStyle){
                        append(" $secondUsername")
                    }
                    
                    withStyle(normalStyle){
                        append(".")
                    }
    
                    addStringAnnotation(
                        tag = "user2",
                        annotation = secondUsername,
                        start = str.indexOf(secondUsername),
                        end = str.lastIndex - 1
                    )
                    
                }
            }
            REVIEW -> {
                buildAnnotatedString {
                    
                    val entityType = feedEvent.metadata.entityType ?: "entity"
    
                    withStyle(normalStyle){
                        append("reviewed ${getArticle(entityType)} ${entityType}.")
                    }
                }
            }
            else -> return emptyString
        }
        
        return firstAnnotatedString.plus(secondAnnotatedString)
    }
    
    companion object {
        
        fun isUserSelf(event: FeedEvent, parentUser: String): Boolean =
            event.username == parentUser
        
        /** @param createdMus is the time event was created in **Microseconds**.*/
        fun getTimeStringForFeed(createdMus: Long): String {
    
            val differenceInSeconds = (System.currentTimeMillis() - createdMus*1000)/1000
            val differenceInMinutes = differenceInSeconds / 60
            val differenceInHours = differenceInMinutes / 60
            
            return when {
                differenceInSeconds == 0L -> "Just now"
                differenceInSeconds in 1..59 -> "$differenceInSeconds second${showPlural(differenceInSeconds)} ago"
                differenceInMinutes in 1..59 -> "$differenceInMinutes minute${showPlural(differenceInMinutes)} ago"
                differenceInHours in 1..23 -> "$differenceInHours hour${showPlural(differenceInHours)} ago"
                else -> TypeConverter.stringFromEpochTime(createdMus)
            }
        }
        
        private fun showPlural(count: Long): String {
            return if (count == 1L) {
                ""
            } else {
                "s"
            }
        }
        
        fun resolveEvent(event: FeedEvent?): FeedEventType =
            when (event?.type) {
                RECORDING_RECOMMENDATION.type -> RECORDING_RECOMMENDATION
                PERSONAL_RECORDING_RECOMMENDATION.type -> PERSONAL_RECORDING_RECOMMENDATION
                RECORDING_PIN.type -> RECORDING_PIN
                LISTEN.type -> LISTEN
                LIKE.type -> LIKE
                FOLLOW.type -> FOLLOW
                NOTIFICATION.type -> NOTIFICATION
                REVIEW.type -> REVIEW
                else -> UNKNOWN
            }
        
        /** This function can be used to determine if an action is delete or hide.*/
        fun isActionDelete(event: FeedEvent, eventType: FeedEventType, parentUser: String): Boolean =
            (isUserSelf(event, parentUser) && eventType.isDeletable) || eventType == NOTIFICATION
    }
    
}