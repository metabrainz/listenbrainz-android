package org.listenbrainz.shared.util

/** Strips the tags from [html] and decodes its entities. */
expect fun htmlToPlainText(html: String): String
