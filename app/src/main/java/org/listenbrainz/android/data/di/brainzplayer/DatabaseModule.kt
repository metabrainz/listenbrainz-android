package org.listenbrainz.android.data.di.brainzplayer

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBrainzPlayerDatabase(
        @ApplicationContext context: Context
    ) : BrainzPlayerDatabase = Room.databaseBuilder(
        context,
        BrainzPlayerDatabase::class.java,
        "brainzplayer_database"
    )
        .build()
}