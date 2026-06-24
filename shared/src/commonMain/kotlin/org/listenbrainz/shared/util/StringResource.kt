package org.listenbrainz.shared.util

enum class StringResource {
    RECOMMENDATION_GREETING,
    PERSONAL_RECOMMENDATION_GREETING,
    REVIEW_GREETING,
    PIN_GREETING,
    LISTEN_DELETED
}

interface StringProvider{
    fun getString(res: StringResource): Int
}