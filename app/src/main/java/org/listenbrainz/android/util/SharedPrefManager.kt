package org.listenbrainz.android.util

import android.content.Context
import android.content.SharedPreferences
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.data.di.brainzplayer.TypeConverter
import org.listenbrainz.android.data.sources.brainzplayer.Playable

class SharedPrefManager(val context: Context) {

    val sharedPreferences : SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, ACCESS_MODE)

    fun setString(context: Context,
    key: String?,
    value: String?) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setInteger(context: Context, key: String?, value: Int) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun setLong(context: Context, key: String?, value: Long) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun setBoolean(context: Context, key: String?, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    var currentPlayable : Playable?
        get() = sharedPreferences.getString(CURRENT_PLAYABLE, "")?.let {
            if (it.isBlank()) null else
                TypeConverter.playableFromJSON(it)
        }
        set(value) {
            value?.let {
                setString(context, TypeConverter.playableToJSON(it), "")
            }
        }

    companion object {
        private const val SHARED_PREFERENCES_NAME = BuildConfig.APPLICATION_ID
        private const val ACCESS_MODE = Context.MODE_PRIVATE
        private const val CURRENT_PLAYABLE = "CURRENT_PLAYABLE"
    }
}