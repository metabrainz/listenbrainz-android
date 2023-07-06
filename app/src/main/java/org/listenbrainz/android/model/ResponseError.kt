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
        
        /** Parsing server response into [ApiError] class. Consider using specific functions like [getSocialResponseError], etc. for each repository if
         * returning errors is the sole motive.*/
        fun <T> parseError(response: Response<T>) : ApiError =
            Gson().fromJson(
                /* json = */ response.errorBody()?.string(),
                /* typeOfT = */ object : TypeToken<ApiError>() {}.type
            )
        
    }
}