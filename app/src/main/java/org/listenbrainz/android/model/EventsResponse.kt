package org.listenbrainz.android.model

data class EventsResponse(
    val events: List<Event>,
    val eventCount: Int,
    val eventOffset: Int
)

data class Event(
    val typeId: String?,
    val time: String?,
    val disambiguation: String?,
    val lifeSpan: LifeSpan,
    val setlist: String?,
    val type: String,
    val name: String,
    val id: String,
    val cancelled: Boolean
)

data class LifeSpan(
    val ended: Boolean,
    val begin: String?,
    val end: String?
)