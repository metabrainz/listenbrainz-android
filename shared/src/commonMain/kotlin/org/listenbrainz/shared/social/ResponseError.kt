package org.listenbrainz.shared.social

sealed class ResponseError {

    object DoesNotExist : ResponseError()
    object BadRequest : ResponseError()
    object AuthHeaderNotFound : ResponseError()
    object RateLimitExceeded : ResponseError()
    object Unauthorised : ResponseError()
    object InternalServerError : ResponseError()
    object BadGateway : ResponseError()
    object RemotePlayerError : ResponseError()
    object ServiceUnavailable : ResponseError()
    object NetworkError : ResponseError()
    object FileNotFound : ResponseError()
    object Unknown : ResponseError()
}