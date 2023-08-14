package org.listenbrainz.android.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.model.AlbumEntity
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.SongEntity
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TypeConverter {

    fun playableToJSON(playable: Playable) = Gson().toJson(playable)!!

    fun playableFromJSON(playableJSON: String) : Playable {
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
    fun artistAlbumsFromJSON(albumsJSON: String): List<AlbumEntity>{
        val type: Type = object: TypeToken<ArrayList<AlbumEntity>>() {}.type
        return Gson().fromJson(
            albumsJSON,
            type
        ) ?: emptyList()
    }
    
    @TypeConverter
    fun nullableListToJSON(list: List<String>?) = Gson().toJson(list)!!
    
    @TypeConverter
    fun nullableListFromJSON(listJSON: String): List<String>? {
        return Gson().fromJson(
            listJSON,
            object: TypeToken<List<String>?>() {}.type
        )
    }
    
    @TypeConverter
    fun stringFromDate(date : Date): String {
        val format = SimpleDateFormat("EEE, d MMM hh:mm aaa", Locale.getDefault())
        return format.format(date)
    }
    
    @TypeConverter
    fun dateFromString(string: String): Date? {
        val format = SimpleDateFormat("EEE, d MMM hh:mm aaa", Locale.getDefault())
        return format.parse(string)
    }
    
    fun stringFromEpochTime(microSeconds: Long, dateFormat: String = "MMM dd, hh:mm aaa"): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        val calendar: Calendar = Calendar.getInstance(Locale.ENGLISH)
            .apply { timeInMillis = microSeconds * 1000 }

        return formatter.format(calendar.time)
    }
    
}