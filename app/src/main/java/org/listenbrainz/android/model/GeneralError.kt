package org.listenbrainz.android.model

enum class GeneralError(override val genericToast: String, override var actualResponse: String? = null): ResponseError {
    
    AUTH_HEADER_NOT_FOUND(genericToast = "Please login in order to perform this operation."),    // "You need to provide an Authorization header.
    
    RATE_LIMIT_EXCEEDED(genericToast = "Server slow down detected."),
    
    NETWORK_ERROR(genericToast = "App is experiencing network issues at the moment."),
    
    UNKNOWN(genericToast = "Some error has occurred.");
    
}