package org.listenbrainz.android.di.brainzplayer

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
    
    @Provides
    @Singleton
    fun providesListenSubmissionDatabase(
        @ApplicationContext context: Context
    ): ListensSubmissionDatabase = Room.databaseBuilder(
        context,
        ListensSubmissionDatabase::class.java,
        "listens_scrobble_database"    // TODO: change this later
    )
        .build()
}
