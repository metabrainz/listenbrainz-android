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
    fun toast(): String = actualResponse ?: genericToast
    
    companion object {
        
        /** Get [ResponseError] for a given Retrofit **error** [Response] from server.*/
        fun <T> getSocialResponseError(response: Response<T>) : ResponseError {
            val error = parseError(response)
            return getSocialErrorType(error)
        }
        
        /** Parsing server response into [ApiError] class. Consider using specific functions like [getSocialResponseError], etc. for each repository if
         * returning errors is the sole motive.*/
        fun <T> parseError(response: Response<T>) : ApiError =
            Gson().fromJson(
                /* json = */ response.errorBody()?.string(),
                /* typeOfT = */ object : TypeToken<ApiError>() {}.type
            )
    
    
        /** Get [ResponseError] for social type API endpoints. Automatically puts actual error message.
         * Prefer using [getSocialResponseError] in repository functions.*/
        private fun getSocialErrorType(apiError: ApiError) : ResponseError {
            val error = apiError.error
            val code = apiError.code
            return when {
                    code == 404 -> SocialError.USER_NOT_FOUND
                    error?.substringAfter(' ')?.contains("is already") == true -> SocialError.ALREADY_FOLLOWING
                    error?.substringAfter(' ') == "cannot follow yourself." -> SocialError.CANNOT_FOLLOW_SELF
                    else -> getGeneralError(error, code)
            }.apply { actualResponse = error }
        }
        
        /** Get [GeneralError] for a given [error] and [code]. Must be used as an extension for all getErrorType functions.*/
        private fun getGeneralError(error: String?, code: Int?) : ResponseError {
            return when {
                error?.slice(12..(error.lastIndex - 8)) == "provide an Authorization" -> GeneralError.AUTH_HEADER_NOT_FOUND
                code == 429 -> GeneralError.RATE_LIMIT_EXCEEDED
                else -> GeneralError.UNKNOWN
            }
        }
    
    }
}