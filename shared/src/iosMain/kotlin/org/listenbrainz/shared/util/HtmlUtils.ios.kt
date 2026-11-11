package org.listenbrainz.shared.util


private val namedEntities = mapOf(
    "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " "
)

private val entityRegex = "&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);".toRegex()

actual fun htmlToPlainText(html: String): String =
    Utils.removeHtmlTags(html).replace(entityRegex) { match ->
        val entity = match.groupValues[1]
        val codePoint = when {
            entity.startsWith("#x") -> entity.drop(2).toIntOrNull(16)
            entity.startsWith("#") -> entity.drop(1).toIntOrNull()
            else -> return@replace namedEntities[entity] ?: match.value
        }
        codePoint?.let(::codePointToString) ?: match.value
    }

private fun codePointToString(codePoint: Int): String? = when {
    codePoint !in 0..0x10FFFF -> null
    codePoint <= 0xFFFF -> Char(codePoint).toString()
    else -> {
        val offset = codePoint - 0x10000
        charArrayOf(Char(0xD800 + (offset shr 10)), Char(0xDC00 + (offset and 0x3FF))).concatToString()
    }
}
