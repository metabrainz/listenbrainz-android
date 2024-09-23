package org.listenbrainz.android.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class SongRepositoryModule {
    @Binds
    abstract fun bindsSongRepository(repository: SongRepositoryImpl?) : SongRepository?
}