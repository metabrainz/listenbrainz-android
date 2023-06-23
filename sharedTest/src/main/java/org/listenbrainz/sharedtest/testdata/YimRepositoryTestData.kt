package org.listenbrainz.sharedtest.testdata

import com.google.gson.Gson
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.sharedtest.utils.EntityTestUtils

object YimRepositoryTestData {
    val testYimData : YimPayload
        get() {
            return Gson().fromJson(EntityTestUtils.loadResourceAsString("yim_data.json"), YimPayload::class.java)
        }
}