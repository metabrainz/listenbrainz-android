package org.listenbrainz.android.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.model.ApiError
import retrofit2.Response

/** These exceptions need to handled in view-model, shown via UI and not just thrown.*/
sealed class LBResponseError(val genericToast: String, var actualResponse: String? = null) {
    
    object UserNotFound : LBResponseError(genericToast = "User not found.")     // "User Some_User_That_Does_Not_Exist not found"
    object AuthHeaderNotFound : LBResponseError(genericToast = "You need to provide an Authorization header."){    // "You need to provide an Authorization header."
        // TODO: Add a composable function that shows dialog to fill token.
    }
    object AlreadyFollowing : LBResponseError(genericToast = "Already following user.")      // "Jasjeet is already following user someotheruser"
    object CannotFollowSelf : LBResponseError(genericToast = "Whoops, cannot follow yourself.")  // "Whoops, cannot follow yourself."
    object Unknown : LBResponseError(genericToast = "Some error has occurred.")
    
}

object ErrorUtil {
    
    fun <T> parseError(response: Response<T>) : ApiError =
        Gson().fromJson(
            /* json = */ response.errorBody()?.string(),
            /* typeOfT = */ object : TypeToken<ApiError>() {}.type
        )
    
    
    /** Get errors only for social type API endpoints.*/
    fun getSocialErrorType(error: String?) : LBResponseError {
        return if (error != null) {
            when {
                error.contains("User") && error.slice((error.lastIndex - 9)..error.lastIndex).contains("not found") -> LBResponseError.UserNotFound
                error.slice(12..(error.lastIndex - 8)) == "provide an Authorization" -> LBResponseError.AuthHeaderNotFound
                error.substringAfter(' ').contains("is already") -> LBResponseError.AlreadyFollowing
                error.substringAfter(' ') == "cannot follow yourself." -> LBResponseError.CannotFollowSelf
                else -> LBResponseError.Unknown
            }
        } else {
            LBResponseError.Unknown
        }
    }
    
}


    



