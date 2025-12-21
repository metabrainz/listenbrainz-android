package org.listenbrainz.android.util

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.listenbrainz.android.model.AlbumEntity
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.SongEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TypeConverter {

    private val json = Json { ignoreUnknownKeys = true }

    fun playableToJSON(playable: Playable): String = json.encodeToString(playable)

    fun playableFromJSON(playableJSON: String): Playable {
        return json.decodeFromString(playableJSON)
    }

    @TypeConverter
    fun playlistToJSON(playlist: List<SongEntity>): String = json.encodeToString(playlist)

    @TypeConverter
    fun playlistFromJSON(playListJSON: String): List<SongEntity> {
        return try {
            json.decodeFromString(playListJSON)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun artistAlbumsToJSON(albums: List<AlbumEntity>): String = json.encodeToString(albums)

    @TypeConverter
    fun artistAlbumsFromJSON(albumsJSON: String): List<AlbumEntity> {
        return try {
            json.decodeFromString(albumsJSON)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun nullableListToJSON(list: List<String>?): String = json.encodeToString(list)
    
    @TypeConverter
    fun nullableListFromJSON(listJSON: String): List<String>? {
        return try {
            json.decodeFromString(listJSON)
        } catch (e: Exception) {
            null
        }
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