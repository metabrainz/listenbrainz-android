package org.listenbrainz.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


interface ApiError {
    val code: Int?
    val error: String?
}

@Serializable
data class ListenBrainzApiError(
    override val code: Int? = null,
    override val error: String? = null
): ApiError

/** Error envelope returned by the YouTube Data API. */
@Serializable
data class YouTubeApiError(
    @SerialName("error")
    val errorBody: Error? = null
): ApiError {

    override val code: Int? get() = errorBody?.code
    override val error get() = errorBody?.message

    @Serializable
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val status: String? = null,
        val errors: List<Detail> = emptyList(),
        val details: List<Info> = emptyList()
    )

    @Serializable
    data class Detail(
        val message: String? = null,
        val domain: String? = null,
        val reason: String? = null
    )

    @Serializable
    data class Info(
        @SerialName("@type") val type: String? = null,
        val reason: String? = null,
        val domain: String? = null,
        val metadata: Map<String, String> = emptyMap(),
        val locale: String? = null,
        val message: String? = null
    )
}
