package org.listenbrainz.shared.model.feed

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.feed_follow
import org.listenbrainz.shared.resources.feed_listen
import org.listenbrainz.shared.resources.feed_love
import org.listenbrainz.shared.resources.feed_notification
import org.listenbrainz.shared.resources.feed_pin
import org.listenbrainz.shared.resources.feed_review
import org.listenbrainz.shared.resources.feed_send
import org.listenbrainz.shared.resources.feed_unknown
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.TypeConverter
import org.listenbrainz.shared.util.Utils.getArticle
import kotlin.time.Clock

/**
 * @param icon Feed icon for the event, **must** be of width 19 dp.
 * @param isDeletable Can only delete our (user's) recommendations and pins.
 * @param isHideable Can only hide followed user's events and notifications.*/
@Immutable
enum class FeedEventType (
    val type: String,
    val icon: DrawableResource,
    val isPlayable: Boolean = true,
    val isDeletable: Boolean = false,
    val isHideable: Boolean = false,
) {
    
    RECORDING_RECOMMENDATION(
        type = "recording_recommendation",
        icon = Res.drawable.feed_send,
        isDeletable = true,
        isHideable = true,
    ),
    
    PERSONAL_RECORDING_RECOMMENDATION(
        type = "personal_recording_recommendation",
        icon = Res.drawable.feed_send,
    ),
    
    RECORDING_PIN(
        type = "recording_pin",
        icon = Res.drawable.feed_pin,
        isDeletable = true,
        isHideable = true,
    ),
    
    LIKE(
        type = "like",
        icon = Res.drawable.feed_love,
    ),
    
    LISTEN(
        type = "listen",
        icon = Res.drawable.feed_listen,
    ),
    
    FOLLOW(
        type = "follow",
        icon = Res.drawable.feed_follow,
        isPlayable = false,
    ),
    
    NOTIFICATION(
        type = "notification",
        icon = Res.drawable.feed_notification,
        isPlayable = false,
        isDeletable = true,
    ),
    
    REVIEW (
        type = "critiquebrainz_review",
        icon = Res.drawable.feed_review,
    ),
    
    /** In case a new event is added in future that had not been published to the app. */
    UNKNOWN(
        type = "update_app",
        icon = Res.drawable.feed_unknown,
        isPlayable = false,
    );
    
    @Composable
    fun Tagline(
        modifier: Modifier = Modifier,
        event: FeedEvent,
        parentUser: String,
        linkStyle: SpanStyle,
        normalStyle: SpanStyle,
        goToUserPage: (String) -> Unit
    ) {
    
        val uriHandler = LocalUriHandler.current
        
        val (annotatedString, onClick) = remember {
            getAnnotatedString(event, parentUser, normalStyle, linkStyle, uriHandler, goToUserPage)
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
        uriHandler: UriHandler,
        goToUserPage: (String) -> Unit
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
                                Log.d(stringAnnotation.item)
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    } catch (e: IllegalArgumentException) {
                        Log.w("MyFeed: Notification link invalid.")
                        e.printStackTrace()
                    } catch (e: Exception) {
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
                val annotatedLinkString = constructTagline(event, parentUser, normalStyle, linkStyle)
                Pair(
                    annotatedLinkString
                ) { charOffset ->
                    annotatedLinkString
                        .getStringAnnotations(charOffset, charOffset)
                        .firstOrNull()
                        ?.let { stringAnnotation ->
                            goToUserPage(stringAnnotation.item)
                        }
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
        val eventUserName = feedEvent.username ?: return emptyString

        val firstUsername = if(isUserSelf(feedEvent, parentUser))
            "You"
        else
            feedEvent.metadata.user0 ?: eventUserName

        val firstAnnotatedString = buildAnnotatedString {
            withStyle(style = linkStyle){
                append("$firstUsername ")
            }
            val annotation = feedEvent.metadata.user0 ?: feedEvent.username ?: ""
            addStringAnnotation(
                tag = "user0",
                annotation = annotation,
                start = 0,
                end = firstUsername.length
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
    
                    // FIXME: Refactor entityType
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
        
        /** @param createdSeconds is the time event was created in **Seconds**.*/
        fun getTimeStringForFeed(createdSeconds: Long): String {
    
            val differenceInSeconds = Clock.System.now().epochSeconds - createdSeconds
            val differenceInMinutes = differenceInSeconds / 60
            val differenceInHours = differenceInMinutes / 60
            
            return when {
                differenceInSeconds == 0L -> "Just now"
                differenceInSeconds in 1..59 -> "$differenceInSeconds second${showPlural(differenceInSeconds)} ago"
                differenceInMinutes in 1..59 -> "$differenceInMinutes minute${showPlural(differenceInMinutes)} ago"
                differenceInHours in 1..23 -> "$differenceInHours hour${showPlural(differenceInHours)} ago"
                else -> TypeConverter.stringFromEpochSeconds(createdSeconds)
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