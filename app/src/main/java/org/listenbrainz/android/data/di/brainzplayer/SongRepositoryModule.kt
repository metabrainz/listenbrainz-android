package org.listenbrainz.android.data.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.repository.SongRepository
import org.listenbrainz.android.data.repository.SongRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class SongRepositoryModule {
    @Binds
    abstract fun bindsSongRepository(repository: SongRepositoryImpl?) : SongRepository?
}