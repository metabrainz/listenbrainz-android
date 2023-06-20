package org.listenbrainz.android.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.model.ApiError
import retrofit2.Response

/** These exceptions need to handled in view-model, shown via UI and not just thrown.*/
enum class ResponseError(private val genericToast: String, var actualResponse: String? = null) {
    
    USER_NOT_FOUND(genericToast = "User not found."),     // "User Some_User_That_Does_Not_Exist not found"
    
    ALREADY_FOLLOWING(genericToast = "Already following user."),     // "Jasjeet is already following user someotheruser"
    
    CANNOT_FOLLOW_SELF(genericToast = "Whoops, cannot follow yourself."),  // "Whoops, cannot follow yourself."
    
    AUTH_HEADER_NOT_FOUND(genericToast = "You need to provide an Authorization header."),    // "You need to provide an Authorization header.
    
    RATE_LIMIT_EXCEEDED(genericToast = "Server slow down detected."),
    
    NETWORK_ERROR(genericToast = "App is experiencing network issues at the moment."),
    
    UNKNOWN(genericToast = "Some error has occurred.");
    
    fun toast(): String = actualResponse ?: genericToast
    
}

object ErrorUtil {
    
    fun <T> parseError(response: Response<T>) : ApiError =
        Gson().fromJson(
            /* json = */ response.errorBody()?.string(),
            /* typeOfT = */ object : TypeToken<ApiError>() {}.type
        )
    
    
    /** Get [ResponseError] for social type API endpoints. Automatically puts actual error message.*/
    fun getSocialErrorType(error: String?) : ResponseError {
        return if (error != null) {
            when {
                error.contains("User") && error.slice((error.lastIndex - 9)..error.lastIndex).contains("not found") -> ResponseError.USER_NOT_FOUND
                error.slice(12..(error.lastIndex - 8)) == "provide an Authorization" -> ResponseError.AUTH_HEADER_NOT_FOUND
                error.substringAfter(' ').contains("is already") -> ResponseError.ALREADY_FOLLOWING
                error.substringAfter(' ') == "cannot follow yourself." -> ResponseError.CANNOT_FOLLOW_SELF
                else -> ResponseError.UNKNOWN
            }.putActualErrorMessage(error)
        } else {
            ResponseError.UNKNOWN
        }
    }
    
    private fun ResponseError.putActualErrorMessage(message: String?): ResponseError =
        this.apply { actualResponse = message }
}


    



