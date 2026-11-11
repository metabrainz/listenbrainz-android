package org.listenbrainz.shared.util

import android.content.Intent
import org.listenbrainz.shared.applicationContext

actual fun shareLink(link: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, link)
    }
    applicationContext.startActivity(
        Intent.createChooser(intent, "Share link via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
