package org.listenbrainz.android.model

import androidx.compose.runtime.Stable
import org.listenbrainz.android.util.Resource

/** These exceptions need to be handled in view-model, shown via UI and not just thrown.
 * @param genericToast Generic message for the error.
 * @param actualResponse Actual response given by the server.*/
@Stable
sealed class ResponseError(open val genericToast: String, open val actualResponse: String? = null) {

    // "User Some_User_That_Does_Not_Exist not found"
    data class DoesNotExist(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Error! Object not found.", actualResponse)

    // "Jasjeet is already following user someotheruser", "Whoops, cannot follow yourself."
    data class BadRequest(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Illegal action.", actualResponse)

    /** Also means the user is not logged in.*/
    // "You need to provide an Authorization header."
    data class AuthHeaderNotFound(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Please login in order to perform this operation.", actualResponse)

    data class RateLimitExceeded(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Rate limit exceeded.", actualResponse)

    data class Unauthorised(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "You are not authorised to access the requested content.", actualResponse)

    data class InternalServerError(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Internal server error.", actualResponse)

    data class BadGateway(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Error! Bad gateway.", actualResponse)

    data class RemotePlayerError(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Error! Could not play the requested listen.", actualResponse)

    data class ServiceUnavailable(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Server outage detected. Please try again later.", actualResponse)

    data class NetworkError(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Network issues detected. Make sure device is connected to internet.", actualResponse)

    data class FileNotFound(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Selected file does not exist.", actualResponse)

    data class Unknown(
        override val actualResponse: String? = null
    ) : ResponseError(genericToast = "Some error has occurred.", actualResponse)

    /** Simple function that returns the most suitable message to show the user.*/
    val toast: String get() = actualResponse ?: genericToast

    /** Wrap this [ResponseError] in [Resource] with an optional parameter to add the actual response.*/
    fun <T> asResource(actualResponse: String? = null): Resource<T> {
        val errorWithResponse = when (this) {
            is DoesNotExist -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is BadRequest -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is AuthHeaderNotFound -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is RateLimitExceeded -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is Unauthorised -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is InternalServerError -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is BadGateway -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is RemotePlayerError -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is ServiceUnavailable -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is NetworkError -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is FileNotFound -> copy(actualResponse = actualResponse ?: this.actualResponse)
            is Unknown -> copy(actualResponse = actualResponse ?: this.actualResponse)
        }
        return Resource.failure(error = errorWithResponse)
    }
}
