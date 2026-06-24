package org.listenbrainz.android.util

import org.listenbrainz.android.R
import org.listenbrainz.shared.util.StringProvider
import org.listenbrainz.shared.util.StringResource

class AppStringProvider(): StringProvider {
    override fun getString(res: StringResource): Int {
        return when(res){
                StringResource.RECOMMENDATION_GREETING -> R.string.recommendation_greeting
                StringResource.PERSONAL_RECOMMENDATION_GREETING -> R.string.personal_recommendation_greeting
                StringResource.PIN_GREETING -> R.string.pin_greeting
                StringResource.REVIEW_GREETING -> R.string.review_greeting
                StringResource.LISTEN_DELETED -> R.string.listen_deleted
            }
    }
}