package org.listenbrainz.android.model

import androidx.compose.runtime.Stable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.error
import retrofit2.Response


/** These exceptions need to be handled in view-model, shown via UI and not just thrown.
 * @param genericToast Generic message for the error.
 * @param actualResponse Actual response given by the server.*/
@Stable
enum class ResponseError(val genericToast: String, var actualResponse: String? = null) {
    
    DOES_NOT_EXIST(genericToast = "Error! Object not found."),     // "User Some_User_That_Does_Not_Exist not found"
    
    BAD_REQUEST(genericToast = "Illegal action."),     // "Jasjeet is already following user someotheruser", "Whoops, cannot follow yourself."
    
    /** Also means the user is not logged in.*/
    AUTH_HEADER_NOT_FOUND(genericToast = "Please login in order to perform this operation."),    // "You need to provide an Authorization header.
    
    RATE_LIMIT_EXCEEDED(genericToast = "Rate limit exceeded."),
    
    UNAUTHORISED(genericToast = "You are not authorised to access the requested content."),
    
    INTERNAL_SERVER_ERROR(genericToast = "Internal server error."),
    
    BAD_GATEWAY(genericToast = "Error! Bad gateway."),
    
    REMOTE_PLAYER_ERROR(genericToast = "Error! Could not play the requested listen."),
    
    SERVICE_UNAVAILABLE(genericToast = "Server outage detected. Please try again later."),
    
    NETWORK_ERROR(genericToast = "Network issues detected. Make sure device is connected to internet."),
    
    FILE_NOT_FOUND(genericToast = "Selected file does not exist."),
    
    UNKNOWN(genericToast = "Some error has occurred.");
    
    
    /** Simple function that returns the most suitable message to show the user.*/
    fun toast(): String = actualResponse ?: genericToast
    
    /** Wrap this [ResponseError] in [Resource] with an optional parameter to add the actual response.*/
    fun <T> asResource(actualResponse: String? = null): Resource<T> =
        Resource.failure(error = this.apply { this@ResponseError.actualResponse = actualResponse })
    
    companion object {
        
        /** Get [ResponseError] for a given Retrofit **error** [Response] from server.*/
        fun <T> getError(response: Response<T>) : ResponseError {
            val apiError = parseError(response)
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
    
        /** Parsing server response into [ApiError] class. Consider using specific functions like [getSocialResponseError], etc. for each repository if
         * returning errors is the sole motive.*/
        fun <T> parseError(response: Response<T>) : ApiError =
            Gson().fromJson(
                /* json = */ response.error(),
                /* typeOfT = */ object : TypeToken<ApiError>() {}.type
            )
        
    }
    
}