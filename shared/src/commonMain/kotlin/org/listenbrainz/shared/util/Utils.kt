package org.listenbrainz.shared.util

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.io.files.FileNotFoundException
import org.listenbrainz.shared.model.ApiError
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.repository.PlatformContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object Utils {

    class PreEmptiveBadRequestException(val responseError: ResponseError) : Exception()
    class KtorRequestScope {
        @OptIn(ExperimentalContracts::class)
        fun failIf(condition: Boolean, error: () -> ResponseError) {
            contract {
                returns() implies !condition
            }
            if (condition) {
                throw PreEmptiveBadRequestException(error())
            }
        }
    }


    /** General function to parse an API endpoint's response executed by Ktor.
     * @param request Call the API endpoint here. Run any pre-conditional checks to directly return error/success in some cases. */
    suspend inline fun <T> parseResponse(request: KtorRequestScope.() -> T): Resource<T> =
        runCatching {
            Resource.success(KtorRequestScope().request())
        }.getOrElse { error ->
            return@getOrElse when (error) {
                is ResponseException -> {
                    val code = error.response.status
                    val actualResponse = error.response.body<ApiError>().error
                    val responseError = when (code) {
                        HttpStatusCode.Companion.BadRequest -> ResponseError.BadRequest(actualResponse)
                        HttpStatusCode.Companion.Unauthorized -> ResponseError.AuthHeaderNotFound(actualResponse)
                        HttpStatusCode.Companion.Forbidden -> ResponseError.Unauthorised(actualResponse)
                        HttpStatusCode.Companion.NotFound -> ResponseError.DoesNotExist(actualResponse)
                        HttpStatusCode.Companion.TooManyRequests -> ResponseError.RateLimitExceeded(actualResponse)
                        HttpStatusCode.Companion.InternalServerError -> ResponseError.InternalServerError(actualResponse)
                        HttpStatusCode.Companion.BadGateway -> ResponseError.BadGateway(actualResponse)
                        HttpStatusCode.Companion.ServiceUnavailable -> ResponseError.ServiceUnavailable(actualResponse)
                        else -> ResponseError.Unknown(actualResponse)
                    }

                    Resource.failure(responseError)
                }
                is PreEmptiveBadRequestException -> Resource.failure(error.responseError)
                else -> logAndReturn(error)
            }
        }

    fun <T> logAndReturn(it: Throwable) : Resource<T> {
        it.printStackTrace()
        return when (it){
            is FileNotFoundException -> Resource.failure(error = ResponseError.FileNotFound())
            is IOException -> Resource.failure(error = ResponseError.NetworkError())
            else -> Resource.failure(error = ResponseError.Unknown())
        }
    }


    /** Get *CoverArtArchive* url for cover art of a release.
     * @param size Allowed sizes are 250, 500, 750 and 1000. Default is 250.*/
    fun getCoverArtUrl(caaReleaseMbid: String?, caaId: Long?, size: Int = 250): String? {
        if (caaReleaseMbid == null || caaId == null) return null
        return "https://archive.org/download/mbid-${caaReleaseMbid}/mbid-${caaReleaseMbid}-${caaId}_thumb${size}.jpg"
    }
}

expect object PlatformUtils {
    fun getSHA1(context: PlatformContext, packageName: String): String?
}