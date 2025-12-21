package org.listenbrainz.android.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

fun JsonElement?.asStringOrNull(): String? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.content
}

fun JsonElement?.asIntOrNull(): Int? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.intOrNull
}

fun JsonElement?.asLongOrNull(): Long? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.longOrNull
}

fun JsonElement?.asDoubleOrNull(): Double? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.doubleOrNull
}

fun JsonElement?.asFloatOrNull(): Float? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.floatOrNull
}

fun JsonElement?.asBooleanOrNull(): Boolean? {
    if (this == null || this is JsonNull) return null
    return (this as? JsonPrimitive)?.booleanOrNull
}
