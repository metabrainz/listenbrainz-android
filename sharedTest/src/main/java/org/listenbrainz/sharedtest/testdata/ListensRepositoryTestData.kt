package org.listenbrainz.sharedtest.testdata

import kotlinx.serialization.json.Json
import org.listenbrainz.android.model.Listens
import org.listenbrainz.sharedtest.utils.ResourceString.listens

object ListensRepositoryTestData {
    private val json = Json { ignoreUnknownKeys = true }
    
    val listensTestData : Listens
        get() = json.decodeFromString(listens)
}