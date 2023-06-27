package org.listenbrainz.android.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

/** These exceptions need to handled in view-model, shown via UI and not just thrown.
 * @param genericToast Generic message for the error.
 * @param actualResponse Actual response given by the server.*/
interface ResponseError {
    
    val genericToast: String
    var actualResponse: String?
    
    /** Simple function that returns the most suitable message to show the user.*/
    fun toast(): String
    
    companion object {
        
        fun <T> parseError(response: Response<T>) : ApiError =
            Gson().fromJson(
                /* json = */ response.errorBody()?.string(),
                /* typeOfT = */ object : TypeToken<ApiError>() {}.type
            )
    
    
        /** Get [ResponseError] for social type API endpoints. Automatically puts actual error message.*/
        fun getSocialErrorType(error: String?, code: Int) : ResponseError {
            return when {
                    code == 404 -> SocialError.USER_NOT_FOUND
                    error?.substringAfter(' ')?.contains("is already") == true -> SocialError.ALREADY_FOLLOWING
                    error?.substringAfter(' ') == "cannot follow yourself." -> SocialError.CANNOT_FOLLOW_SELF
                    else -> getGeneralError(error, code)
                }.putActualErrorMessage(error)
        }
        
        /** Extension function for all getErrorType functions.*/
        private fun getGeneralError(error: String?, code: Int) : ResponseError {
            return when {
                error?.slice(12..(error.lastIndex - 8)) == "provide an Authorization" -> GeneralError.AUTH_HEADER_NOT_FOUND
                code == 429 -> GeneralError.RATE_LIMIT_EXCEEDED
                else -> GeneralError.UNKNOWN
            }
        }
    
        private fun ResponseError.putActualErrorMessage(message: String?) : ResponseError =
            this.apply { actualResponse = message }
    }
}