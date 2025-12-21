package org.listenbrainz.sharedtest.testdata

import kotlinx.serialization.json.Json
import org.listenbrainz.android.model.yimdata.YimPayload
import org.listenbrainz.sharedtest.utils.ResourceString.yim_data

object YimRepositoryTestData {
    private val json = Json { ignoreUnknownKeys = true }
    
    val testYimData : YimPayload
        get() = json.decodeFromString(yim_data)
}