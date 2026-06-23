package org.listenbrainz.shared.di.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.listenbrainz.shared.model.ListenSubmitBody
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.util.TypeConverter

@Database(
    entities = [
        ListenSubmitBody.Payload::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
@ConstructedBy(ListensSubmissionDatabaseConstructor::class)
abstract class ListensSubmissionDatabase: RoomDatabase() {

    abstract fun pendingListensDao() : PendingListensDao

}

@Suppress("KotlinNoActualForExpect")
expect object ListensSubmissionDatabaseConstructor: RoomDatabaseConstructor<ListensSubmissionDatabase>{
    override fun initialize(): ListensSubmissionDatabase
}