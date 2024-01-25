package org.listenbrainz.android.repository.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.IS_LISTENING_ALLOWED
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_APPS
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_WHITELIST
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.THEME
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Constants.ONBOARDING
import org.listenbrainz.android.util.Constants.Strings.CURRENT_PLAYABLE
import org.listenbrainz.android.util.Constants.Strings.LB_ACCESS_TOKEN
import org.listenbrainz.android.util.Constants.Strings.LINKED_SERVICES
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_ALBUMS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_APPS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_BLACKLIST
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_WHITELIST
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTEN_NEW_PLAYERS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_PERMS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SONGS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SUBMIT_LISTENS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_THEME
import org.listenbrainz.android.util.Constants.Strings.REFRESH_TOKEN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.Constants.Strings.USERNAME
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.TypeConverter
import org.listenbrainz.android.util.datastore.Preference.Companion.ComplexPreference
import org.listenbrainz.android.util.datastore.DataStoreSerializers.linkedServicesListSerializer
import org.listenbrainz.android.util.datastore.DataStoreSerializers.stringListSerializer
import org.listenbrainz.android.util.datastore.DataStoreSerializers.themeSerializer
import org.listenbrainz.android.util.datastore.Preference.Companion.PrimitivePreference
import org.listenbrainz.android.util.datastore.ProtoDataStore
import org.listenbrainz.android.util.datastore.migrations.blacklistMigration

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        // Since we're migrating from SharedPreferences, add a migration based on the
        // SharedPreferences name
        listOf(SharedPreferencesMigration(
            context,
            context.packageName + "_preferences",
            setOf(
                LB_ACCESS_TOKEN,
                USERNAME,
                PREFERENCE_SYSTEM_THEME,
                PREFERENCE_LISTENING_APPS
            )
        ), blacklistMigration
        )
    }
)

class AppPreferencesImpl(private val context: Context) : ProtoDataStore(context.dataStore), AppPreferences {
    companion object {
        object PreferenceKeys {
            val LB_ACCESS_TOKEN = stringPreferencesKey(Constants.Strings.LB_ACCESS_TOKEN)
            val USERNAME = stringPreferencesKey(Constants.Strings.USERNAME)
            val LISTENING_BLACKLIST = stringPreferencesKey(PREFERENCE_LISTENING_BLACKLIST)
            val LISTENING_WHITELIST = stringPreferencesKey(PREFERENCE_LISTENING_WHITELIST)
            val THEME = stringPreferencesKey(PREFERENCE_SYSTEM_THEME)
            val LISTENING_APPS = stringPreferencesKey(PREFERENCE_LISTENING_APPS)
            val IS_LISTENING_ALLOWED = booleanPreferencesKey(PREFERENCE_SUBMIT_LISTENS)
            val SHOULD_LISTEN_NEW_PLAYERS = booleanPreferencesKey(PREFERENCE_LISTEN_NEW_PLAYERS)
        }
    }
    
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // Helper Functions
    
    private fun setString(key: String?, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
    private fun setInteger(key: String?, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }
    private fun setLong(key: String?, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }
    private fun setBoolean(key: String?, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
    
    // Preferences Implementation
    
    override val themePreference: ComplexPreference<UiMode>
        get() = object : ComplexDataStorePreference<UiMode>(
            key = THEME,
            serializer = themeSerializer
        ) {}
    
    override var permissionsPreference: String?
        get() = preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        set(value) = setString(PREFERENCE_PERMS, value)
    
    override val listeningWhitelist: ComplexPreference<List<String>>
        get() = object: ComplexDataStorePreference<List<String>>(
            key = LISTENING_WHITELIST,
            serializer = stringListSerializer
        ) {}

    override val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(context.packageName)
        }
    
    override val isListeningAllowed: PrimitivePreference<Boolean>
        get() = object: PrimitiveDataStorePreference<Boolean>(
            key = IS_LISTENING_ALLOWED,
            defaultValue = true
        ) {}
    
    override val shouldListenNewPlayers: PrimitivePreference<Boolean>
        get() = object: PrimitiveDataStorePreference<Boolean>(
        key = SHOULD_LISTEN_NEW_PLAYERS,
        defaultValue = true
    ) {}
    
    override val listeningApps: ComplexPreference<List<String>>
        get() = object: ComplexDataStorePreference<List<String>>(
            key = LISTENING_APPS,
            serializer = stringListSerializer
        ) {}

    override val version: String
        get() = try {
            context.packageManager?.getPackageInfo(context.packageName, 0)!!.versionName
        }
        catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }

    override var onboardingCompleted: Boolean
        get() = preferences.getBoolean(ONBOARDING, false)
        set(value) = setBoolean(ONBOARDING, value)
    
    override suspend fun logoutUser(): Unit = withContext(Dispatchers.IO) {
        val editor = preferences.edit()
        editor.remove(REFRESH_TOKEN)
        editor.remove(USERNAME)
        editor.apply()
        lbAccessToken.set("")
    }
    
    override var currentPlayable : Playable?
        get() = preferences.getString(CURRENT_PLAYABLE, "")?.let {
            if (it.isBlank()) null else
                TypeConverter.playableFromJSON(it)
        }
        set(value) {
            value?.let {
                setString(CURRENT_PLAYABLE, TypeConverter.playableToJSON(it))
            }
        }

    /* Login Preferences */
    
    override fun getLoginStatusFlow(): Flow<Int> =
         lbAccessToken.getFlow().map { token ->
             if (token.isNotEmpty())
                 STATUS_LOGGED_IN
             else
                 STATUS_LOGGED_OUT
         }.distinctUntilChanged()
    
    override suspend fun isUserLoggedIn() : Boolean =
        lbAccessToken.get().isNotEmpty()
    
    override val lbAccessToken: PrimitivePreference<String>
        get() = object : PrimitiveDataStorePreference<String>(
            key = PreferenceKeys.LB_ACCESS_TOKEN,
            defaultValue = ""
        ) {}
    
    override val username: PrimitivePreference<String>
        get() = object : PrimitiveDataStorePreference<String>(
            key = PreferenceKeys.USERNAME,
            defaultValue = ""
        ) {}
    
    override var linkedServices: List<LinkedService>
        get() {
            val jsonString = preferences.getString(LINKED_SERVICES, "") ?: ""
            return linkedServicesListSerializer.from(jsonString)
        }
        set(value) {
            val jsonString = linkedServicesListSerializer.to(value)
            setString(LINKED_SERVICES, jsonString)
        }

    override val refreshToken: String?
        get() = preferences.getString(REFRESH_TOKEN, "")
    
    /* BrainzPlayer Preferences */
    
    override var albumsOnDevice: Boolean
        get() = preferences.getBoolean(PREFERENCE_ALBUMS_ON_DEVICE, true)
        set(value) = setBoolean(PREFERENCE_ALBUMS_ON_DEVICE, value)
    
    override var songsOnDevice: Boolean
        get() = preferences.getBoolean(PREFERENCE_SONGS_ON_DEVICE, true)
        set(value) = setBoolean(PREFERENCE_SONGS_ON_DEVICE, value)
}