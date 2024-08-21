package org.listenbrainz.sharedtest.testdata

import com.google.gson.Gson
import org.listenbrainz.android.model.Listens
import org.listenbrainz.sharedtest.utils.ResourceString.listens

object ListensRepositoryTestData {
    val listensTestData : Listens
        get() = Gson().fromJson(listens, Listens::class.java)
}