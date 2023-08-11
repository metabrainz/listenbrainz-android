package org.listenbrainz.sharedtest.testdata

import com.google.gson.Gson
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.sharedtest.utils.ResourceString.yim_data

object YimRepositoryTestData {
    val testYimData : YimPayload
        get() = Gson().fromJson(yim_data, YimPayload::class.java)
}