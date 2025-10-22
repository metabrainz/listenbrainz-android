package org.listenbrainz.android.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import javax.inject.Inject

class FeedServiceKtorImpl @Inject constructor(
    private val httpClient: HttpClient
) : FeedServiceKtor {

    override suspend fun getFeedEvents(
        username: String,
        count: Int,
        maxTs: Int?,
        minTs: Int?
    ): FeedData {
        return httpClient.get("user/$username/feed/events") {
            url {
                parameters.append("count", count.toString())
                maxTs?.let { parameters.append("max_ts", it.toString()) }
                minTs?.let { parameters.append("min_ts", it.toString()) }
            }
        }.body()
    }

    override suspend fun getFeedFollowListens(
        username: String,
        count: Int,
        maxTs: Int?,
        minTs: Int?
    ): FeedData {
        return httpClient.get("user/$username/feed/events/listens/following") {
            url {
                parameters.append("count", count.toString())
                maxTs?.let { parameters.append("max_ts", it.toString()) }
                minTs?.let { parameters.append("min_ts", it.toString()) }
            }
        }.body()
    }

    override suspend fun getFeedSimilarListens(
        username: String,
        count: Int,
        maxTs: Int?,
        minTs: Int?
    ): FeedData {
        return httpClient.get("user/$username/feed/events/listens/similar") {
            url {
                parameters.append("count", count.toString())
                maxTs?.let { parameters.append("max_ts", it.toString()) }
                minTs?.let { parameters.append("min_ts", it.toString()) }
            }
        }.body()
    }

    override suspend fun deleteEvent(
        username: String,
        body: FeedEventDeletionData
    ): SocialResponse {
        return httpClient.post("user/$username/feed/events/delete") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    override suspend fun hideEvent(
        username: String,
        body: FeedEventVisibilityData
    ): SocialResponse {
        return httpClient.post("user/$username/feed/events/hide") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    override suspend fun unhideEvent(
        username: String,
        body: FeedEventVisibilityData
    ): SocialResponse {
        return httpClient.post("user/$username/feed/events/unhide") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }
}
