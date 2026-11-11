package org.listenbrainz.shared.util

import androidx.core.text.HtmlCompat

actual fun htmlToPlainText(html: String): String =
    HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
