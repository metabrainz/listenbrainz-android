package org.listenbrainz.android.model

import retrofit2.Response

enum class Error(override val genericToast: String, override var actualResponse: String? = null) : ResponseError {
    
    DOES_NOT_EXIST(genericToast = "Error! Object not found."),     // "User Some_User_That_Does_Not_Exist not found"
    
    BAD_REQUEST(genericToast = "Already following user."),     // "Jasjeet is already following user someotheruser"
    
    AUTH_HEADER_NOT_FOUND(genericToast = "Please login in order to perform this operation."),    // "You need to provide an Authorization header.
    
    RATE_LIMIT_EXCEEDED(genericToast = "Rate limit exceeded."),
    
    UNAUTHORISED(genericToast = "You are not authorised to access the requested content."),
    
    INTERNAL_SERVER_ERROR(genericToast = "Internal server error."),
    
    BAD_GATEWAY(genericToast = "Error! Bad gateway."),
    
    SERVICE_UNAVAILABLE(genericToast = "Server outage detected."),
    
    NETWORK_ERROR(genericToast = "App is experiencing network issues at the moment."),
    
    FILE_NOT_FOUND(genericToast = "Selected file does not exist."),
    
    UNKNOWN(genericToast = "Some error has occurred.");
    
    companion object {
        
        /** Get [ResponseError] for a given Retrofit **error** [Response] from server.*/
        fun <T> getError(response: Response<T>) : ResponseError {
            val apiError = ResponseError.parseError(response)
            val error = apiError.error
            val code = apiError.code
            return when (code) {
                400 -> BAD_REQUEST
                401 -> AUTH_HEADER_NOT_FOUND
                403 -> UNAUTHORISED
                404 -> DOES_NOT_EXIST
                429 -> RATE_LIMIT_EXCEEDED
                500 -> INTERNAL_SERVER_ERROR
                502 -> BAD_GATEWAY
                503 -> SERVICE_UNAVAILABLE
                else -> UNKNOWN
            }.apply { actualResponse = error }
        }
        
    }
    
}