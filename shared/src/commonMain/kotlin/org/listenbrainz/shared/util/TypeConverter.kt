package org.listenbrainz.shared.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.listenbrainz.shared.model.AlbumEntity
import org.listenbrainz.shared.model.Playable
import org.listenbrainz.shared.model.SongEntity

object TypeConverter {

    private val json = Json { ignoreUnknownKeys = true }

    fun playableToJSON(playable: Playable): String = json.encodeToString(playable)

    fun playableFromJSON(playableJSON: String): Playable {
        return json.decodeFromString(playableJSON)
    }

    @androidx.room.TypeConverter
    fun playlistToJSON(playlist: List<SongEntity>): String = json.encodeToString(playlist)

    @androidx.room.TypeConverter
    fun playlistFromJSON(playListJSON: String): List<SongEntity> {
        return try {
            json.decodeFromString(playListJSON)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @androidx.room.TypeConverter
    fun artistAlbumsToJSON(albums: List<AlbumEntity>): String = json.encodeToString(albums)

    @androidx.room.TypeConverter
    fun artistAlbumsFromJSON(albumsJSON: String): List<AlbumEntity> {
        return try {
            json.decodeFromString(albumsJSON)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @androidx.room.TypeConverter
    fun nullableListToJSON(list: List<String>?): String = json.encodeToString(list)

    @androidx.room.TypeConverter
    fun nullableListFromJSON(listJSON: String): List<String>? {
        return try {
            json.decodeFromString(listJSON)
        } catch (e: Exception) {
            null
        }
    }

    private val dateFormatter = LocalDateTime.Format {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
        char(',')
        char(' ')
        dayOfMonth(Padding.NONE)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        amPmHour(Padding.NONE)
        char(':')
        minute(Padding.ZERO)
        char(' ')
        amPmMarker("AM","PM")
    }

    @androidx.room.TypeConverter
    fun stringFromDate(date: LocalDateTime): String {
        return date.format(dateFormatter)
    }

    @androidx.room.TypeConverter
    fun dateFromString(string: String): LocalDateTime? {
        if(string.isBlank()){
            return null
        }
        return try {
            LocalDateTime.parse(string,dateFormatter)
        } catch (e: Exception){
            null
        }
    }

    fun stringFromEpochTime(microSeconds: Long, dateFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth(Padding.ZERO)
        char(',')
        char(' ')
        amPmHour(Padding.NONE)
        char(':')
        minute(Padding.ZERO)
        char(' ')
        amPmMarker("AM","PM")
    }): String {
        val instant = Instant.fromEpochMilliseconds(microSeconds/1000)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.format(dateFormat)
    }

}