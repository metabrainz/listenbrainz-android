package org.listenbrainz.android.data.dao

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.di.database.Migrations
import org.listenbrainz.shared.model.AlbumEntity
import org.listenbrainz.shared.model.ArtistEntity
import org.listenbrainz.shared.model.PlaylistEntity
import org.listenbrainz.shared.model.SongEntity
import org.listenbrainz.shared.model.dao.AlbumDao
import org.listenbrainz.shared.model.dao.ArtistDao
import org.listenbrainz.shared.model.dao.PlaylistDao
import org.listenbrainz.shared.model.dao.SongDao
import org.listenbrainz.sharedtest.utils.CoroutineTestRule


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DaoTest {

    @get:Rule
    val dispatcherRule = CoroutineTestRule()

    private lateinit var albumDao: AlbumDao
    private lateinit var artistDao: ArtistDao
    private lateinit var playlistDao: PlaylistDao
    private lateinit var songDao: SongDao
    private lateinit var brainzPlayerDatabase: BrainzPlayerDatabase

    private val albumEntities = (1..10).map { album ->
        AlbumEntity(
            album.toLong(),
            "Album$album",
            "Artist$album",
            "AlbumArt$album",
        )
    }

    private val songEntities = (11..20).map { song->
        SongEntity(
            song.toLong(),
            "Song$song",
            "Artist$song",
            "Uri$song",
            song.toLong(),
            "Album$song",
            "AlbumArt$song",
             song,
             song,
             song.toLong(),
             song.toLong(),
             song.toLong(),
             song.toLong(),
            0
        )
    }

    private val playlistEntities = (101..110).map {
        PlaylistEntity(
            it.toLong(),
            "Title$it",
            (11..20).map { song ->
                SongEntity(
                    song.toLong(),
                    "SongTitle$song",
                    "Artist$song",
                    "Uri$song",
                    song.toLong(),
                    "Album$song",
                    "AlbumArt$song",
                    song,
                    song,
                    song.toLong(),
                    song.toLong(),
                    song.toLong(),
                    song.toLong(),
                    song.toLong()
                )
            }
        )
    }

    private val artistEntities = (111..120).map {
        ArtistEntity(
            it.toLong(),
            "Artist$it",
            (121..130).map { song ->
                SongEntity(
                    song.toLong(),
                    "SongTitle$song",
                    "Artist$it",
                    "Uri$song",
                    (song + 10).toLong(),
                    "Album$song",
                    "AlbumArt$song",
                    song,
                    song,
                    song.toLong(),
                    song.toLong(),
                    song.toLong(),
                    song.toLong(),
                    song.toLong()
                )

            }.toMutableList(),
            (131..140).map { album ->
                AlbumEntity(
                    album.toLong(),
                    "Album$album",
                    "Artist$it",
                    "AlbumArt$album",
                )
            }.toMutableList()
        )
    }

    @Before
    fun create() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        brainzPlayerDatabase = Room
            .inMemoryDatabaseBuilder(context, BrainzPlayerDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3)
            .build()
        albumDao = brainzPlayerDatabase.albumDao()
        artistDao = brainzPlayerDatabase.artistDao()
        playlistDao = brainzPlayerDatabase.playlistDao()
        songDao = brainzPlayerDatabase.songDao()
    }


    @Test
    fun albumDaoTests() = runTest {
        albumDao.addAlbums(albumEntities)
        albumDao.getAlbumEntity(1).test {
            val album = awaitItem()
            assertTrue(album.albumId == (1).toLong())
            cancel()
        }
        albumDao.getAlbumEntities().test {
            val albumItems = awaitItem()
            assertTrue(albumItems.size == 10)
            cancel()
        }
    }

    @Test
    fun playlistTests() = runTest {
        val playlistSize = playlistEntities.size
        playlistDao.insertPlaylists(playlistEntities)
        playlistDao.getPlaylist((102).toLong()).test {
            val playlist = awaitItem()
            assertTrue(playlist.id == 102.toLong())
            assertTrue(playlist.items.isNotEmpty())
            playlistDao.delete(playlist)

        }
        playlistDao.getAllPlaylist().test {
            val playlists = awaitItem()
            assertTrue(playlists.size == playlistSize - 1)
            val playlist = playlists[0]
            val targetPlaylistSize = playlist.items.size
            playlistDao.renamePlaylistName("newName", playlist.id)
            playlistDao.updatePlaylistSongs(
                playlist.items.subList(0, targetPlaylistSize - 1),
                playlist.id
            )
            playlistDao.getPlaylist((101).toLong()).test {
                val thisPlaylist = awaitItem()
                assertTrue(thisPlaylist.title == "newName")
                assertTrue(thisPlaylist.items.size == targetPlaylistSize - 1)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun artistTests() = runTest {
        artistDao.addArtists(artistEntities)
        artistDao.getArtistEntity((111).toString()).test {
            val artist = awaitItem()
            assertTrue(artist.songs.isNotEmpty())
            assertTrue(artist.albums.isNotEmpty())
        }
        artistDao.getArtistEntities().test {
            val artists = awaitItem()
            assertTrue(artists.size == 10)
        }
    }

    @Test
    fun songTest() = runTest {
        songDao.addSongs(songEntities)
        songDao.getSongEntity((11).toString()).test {
            val song = awaitItem()
            assertTrue(song.mediaID == (11).toLong())
        }
        songDao.getSongEntities().test {
            val songs = awaitItem()
            assertTrue(songs.size == 10)
        }
    }

    @After
    fun cleanup() {
        brainzPlayerDatabase.close()
    }
}