package org.listenbrainz.android.data.di.brainzplayer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.data.sources.brainzplayer.AlbumEntity
import org.listenbrainz.android.data.sources.brainzplayer.Playable
import org.listenbrainz.android.data.sources.brainzplayer.SongEntity
import java.lang.reflect.Type

object TypeConverter {

    fun playableToJSON(playable: Playable) = Gson().toJson(playable)!!

    fun playableFromJSON(playableJSON: String) : Playable{
        val type: Type = object: TypeToken<Playable>() {}.type
        return Gson().fromJson(
            playableJSON,
            type
        )
    }

    @TypeConverter
    fun playlistToJSON(playlist: List<SongEntity>) = Gson().toJson(playlist)!!

    @TypeConverter
    fun playlistFromJSON(playListJSON: String): List<SongEntity>{
        val type: Type = object: TypeToken<ArrayList<SongEntity>>() {}.type
        return Gson().fromJson(
            playListJSON,
            type
        ) ?: emptyList()
    }

    @TypeConverter
    fun artistAlbumsToJSON(albums: List<AlbumEntity>) = Gson().toJson(albums)!!

    @TypeConverter
    fun artistAlbumsToJSON(albumsJSON: String): List<AlbumEntity>{
        val type: Type = object: TypeToken<ArrayList<AlbumEntity>>() {}.type
        return Gson().fromJson(
            albumsJSON,
            type
        ) ?: emptyList()
    }
}