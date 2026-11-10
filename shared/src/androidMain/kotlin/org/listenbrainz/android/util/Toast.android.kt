package org.listenbrainz.android.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import org.listenbrainz.shared.applicationContext

actual fun showToast(message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
