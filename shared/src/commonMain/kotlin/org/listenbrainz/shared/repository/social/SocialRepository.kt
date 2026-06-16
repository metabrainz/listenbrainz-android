package org.listenbrainz.shared.repository.social

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.listenbrainz.shared.model.PinData
import org.listenbrainz.shared.model.RecommendationData
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.SearchResult
import org.listenbrainz.shared.model.SimilarUserData
import org.listenbrainz.shared.model.SocialData
import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.util.Resource
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Instant
import kotlin.time.Clock

interface SocialRepository {
    
    suspend fun getFollowers(username: String?) : Resource<SocialData>
    
    suspend fun getFollowing(username: String) : Resource<SocialData>
    
    suspend fun followUser(username: String) : Resource<SocialResponse>
    
    suspend fun unfollowUser(username: String) : Resource<SocialResponse>
    
    suspend fun getSimilarUsers(username: String) : Resource<SimilarUserData>
    
    suspend fun searchUser(username: String) : Resource<SearchResult>
    
    suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postReview(username: String?, data: Review): Resource<FeedEvent>
    
    suspend fun pin(recordingMsid: String?, recordingMbid: String?, blurbContent: String?, pinnedUntil: Int = (getPinTimeMs() /1000).toInt()): Resource<PinData>
    
    suspend fun deletePin(id: Int): Resource<SocialResponse>
    
    companion object {
        val formatter = LocalDateTime.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            dayOfMonth(Padding.ZERO)
            char(',')
            char(' ')
            amPmHour(Padding.ZERO)
            char(':')
            minute(Padding.ZERO)
            char(' ')
            amPmMarker("AM","PM")
        }
        fun getPinDateString(): String {
            val pinTimeMs = getPinTimeMs()
            val instant = Instant.fromEpochMilliseconds(pinTimeMs)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
            return formatter.format(localDateTime)
        }
    
        fun getPinTimeMs(): Long = (Clock.System.now() + 7.days).toEpochMilliseconds()
    }
}