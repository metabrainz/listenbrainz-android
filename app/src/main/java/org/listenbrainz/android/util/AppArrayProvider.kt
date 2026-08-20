package org.listenbrainz.android.util

import org.listenbrainz.android.R
import org.listenbrainz.shared.util.ArrayProvider
import org.listenbrainz.shared.util.ArrayResource

class AppArrayProvider(): ArrayProvider {
    override fun getArray(res: ArrayResource): Int {
        return when(res){
            ArrayResource.NOTIFICATION_IDLE_MESSAGES -> R.array.notification_idle_messages
        }
    }
}