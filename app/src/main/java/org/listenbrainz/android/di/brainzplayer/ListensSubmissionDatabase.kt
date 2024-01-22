package org.listenbrainz.android.di.brainzplayer

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.dao.PendingListensDao
import org.listenbrainz.android.util.TypeConverter

@Database(
    entities = [
        ListenSubmitBody.Payload::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class ListensSubmissionDatabase: RoomDatabase() {
    
    abstract fun pendingListensDao() : PendingListensDao
    
}