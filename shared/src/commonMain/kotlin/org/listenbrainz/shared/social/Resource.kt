package org.listenbrainz.shared.social


data class Resource<T>(
    val status: Status,
    val data: T?,
    val error: ResponseError? = null
) {

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

        fun <S> failure(error: ResponseError? = null, data: S? = null): Resource<S> =
            Resource(Status.FAILED, data, error)

        fun <S> loading(data: S? = null): Resource<S> =
            Resource(Status.LOADING, data)
    }
}