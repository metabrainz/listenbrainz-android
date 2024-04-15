package org.listenbrainz.android.util.datastore

import com.jasjeet.typesafe_datastore.DataStoreSerializer
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.model.UiMode.Companion.asUiMode

object DataStoreSerializers {
    val themeSerializer: DataStoreSerializer<UiMode, String>
        get() = object: DataStoreSerializer<UiMode, String> {
            override fun from(value: String): UiMode = value.asUiMode()
            
            override fun to(value: UiMode): String = value.name
            
            override fun default(): UiMode = UiMode.FOLLOW_SYSTEM
        }
}