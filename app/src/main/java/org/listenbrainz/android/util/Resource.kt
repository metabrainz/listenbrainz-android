package org.listenbrainz.android.util

class Resource<T>(val status: Status, val data: T?) {

    enum class Status {
        LOADING, FAILED, SUCCESS
    }

    companion object {
        fun <S> success(data: S): Resource<S> =
            Resource(Status.SUCCESS, data)
        
        fun <S> failure(data: S? = null): Resource<S> =
            Resource(Status.FAILED, data)
        
        fun <S> loading(data: S? = null): Resource<S> =
            Resource(Status.LOADING, null)
    }
}