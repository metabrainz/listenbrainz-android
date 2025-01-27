package org.listenbrainz.android.util

import org.listenbrainz.android.model.ResponseError

/**Use this class to pass [data] and [status] to view-model.
 * @param error Whenever an error is occurred, the error must be passed to view-model through this parameter. *null* means no error.*/
class Resource<T>(val status: Status, val data: T?, val error: ResponseError? = null) {

    inline val isSuccess get() = status.isSuccessful()
    inline val isFailed get() = status.isFailed()
    inline val isLoading get() = status.isLoading()

    enum class Status {
        LOADING, FAILED, SUCCESS;
        
        fun isSuccessful() = this == SUCCESS

        fun isFailed() = this == FAILED

        fun isLoading() = this == LOADING

    }

    companion object {
        fun <S> success(data: S): Resource<S> =
            Resource(Status.SUCCESS, data)
        
        /** Return [ResponseError] if any.*/
        fun <S> failure(data: S? = null, error: ResponseError? = null): Resource<S> =
            Resource(Status.FAILED, data, error)
        
        fun <S> loading(data: S? = null): Resource<S> =
            Resource(Status.LOADING, data)
    }
}