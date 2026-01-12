package org.listenbrainz.android.repository.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.model.UiMode.Companion.asUiMode
import org.listenbrainz.shared.preferences.PreferenceKeys
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.TypeConverter
import org.listenbrainz.shared.preferences.DATA_STORE_FILE_NAME
import org.listenbrainz.shared.preferences.DataStorePreference
import org.listenbrainz.shared.preferences.createDataStore

class AppPreferencesImpl(private val context: Context): AppPreferences {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        // Legacy key - only used for one-time migration cleanup
        private const val LEGACY_PERMS_KEY = "perms_code"
        
        private val permsMigration: DataMigration<Preferences> =
            object : DataMigration<Preferences> {
                override suspend fun cleanUp() = Unit
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    return currentData.contains(stringPreferencesKey(LEGACY_PERMS_KEY))
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    val mutablePreferences = currentData.toMutablePreferences()
                    mutablePreferences.remove(stringPreferencesKey(LEGACY_PERMS_KEY))
                    Log.i("AppPreferencesImpl", "Removed old permissions key: $LEGACY_PERMS_KEY")
                    return mutablePreferences.toPreferences()
                }
            }
        private val blacklistMigration: DataMigration<Preferences> =
            object : DataMigration<Preferences> {
                override suspend fun cleanUp() = Unit

                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    // If blacklist is deleted, then we are sure that migration took place.
                    return currentData.contains(PreferenceKeys.LISTENING_BLACKLIST)
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    val blacklist = currentData[PreferenceKeys.LISTENING_BLACKLIST].asStringList()
                    val appList = currentData[PreferenceKeys.LISTENING_APPS].asStringList()

                    val whitelist = currentData[PreferenceKeys.LISTENING_WHITELIST].asStringList().toMutableSet()
                    appList.forEach { pkg ->
                        if (!blacklist.contains(pkg)) {
                            whitelist.add(pkg)
                        }
                    }

                    val mutablePreferences = currentData.toMutablePreferences()
                    mutablePreferences[PreferenceKeys.LISTENING_WHITELIST] = json.encodeToString(whitelist.toList())
                    mutablePreferences.remove(PreferenceKeys.LISTENING_BLACKLIST)  // Clear old stale data and key.

                    return mutablePreferences.toPreferences()
                }
            }
        private fun sharedPrefsMigrations(context: Context): List<DataMigration<Preferences>> =
            listOf(
                SharedPreferencesMigration(
                    context,
                    context.packageName + "_preferences",
                    setOf(
                        PreferenceKeys.LB_ACCESS_TOKEN.name,
                        PreferenceKeys.USERNAME.name,
                        PreferenceKeys.REFESH_TOKEN.name,
                        PreferenceKeys.LINKED_SERVICES.name,
                        PreferenceKeys.THEME.name,
                        PreferenceKeys.LISTENING_APPS.name,
                        PreferenceKeys.LISTENING_BLACKLIST.name,
                        PreferenceKeys.LISTENING_WHITELIST.name,
                        PreferenceKeys.IS_LISTENING_ALLOWED.name,
                        PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS.name,
                        PreferenceKeys.PERMISSIONS_REQUESTED.name,
                        PreferenceKeys.CONSENT_SCREEN_CACHE.name,
                        PreferenceKeys.INSTALL_SOURCE.name,
                        PreferenceKeys.APP_LAUNCH_COUNT.name,
                        PreferenceKeys.LAST_VERSION_CHECK_LAUNCH_COUNT.name,
                        PreferenceKeys.LAST_UPDATE_PROMPT_LAUNCH_COUNT.name,
                        PreferenceKeys.GITHUB_DOWNLOAD_ID.name,
                        PreferenceKeys.ALBUMS_ON_DEVICE.name,
                        PreferenceKeys.SONGS_ON_DEVICE.name,
                        PreferenceKeys.CURRENT_PLAYABLE.name,
                        PreferenceKeys.ONBOARDING.name
                    )
                ),
                blacklistMigration,
                permsMigration
            )

        @Volatile
        private var sharedDataStore: DataStore<Preferences>? = null

        private fun getSharedDataStore(context: Context): DataStore<Preferences> {
            val existing = sharedDataStore
            if (existing != null) return existing
            return synchronized(this) {
                val cached = sharedDataStore
                if (cached != null) {
                    cached
                } else {
                    val appContext = context.applicationContext
                    val created = createDataStore(
                        producePath = {
                            appContext.filesDir.resolve("datastore/$DATA_STORE_FILE_NAME").absolutePath
                        },
                        migrations = sharedPrefsMigrations(appContext)
                    )
                    sharedDataStore = created
                    created
                }
            }
        }

        fun String?.asStringList(): List<String> {
            return try {
                if (this.isNullOrEmpty()) emptyList()
                else json.decodeFromString(this)
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun String?.asLinkedServiceList(): List<LinkedService> {
            return try {
                if (this.isNullOrEmpty()) emptyList()
                else json.decodeFromString(this)
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun String?.asPlayable(): Playable? {
            return try {
                if (this.isNullOrBlank()) null
                else TypeConverter.playableFromJSON(this)
            } catch (e: Exception) {
                null
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val dataStore: DataStore<Preferences> = getSharedDataStore(context)

    private val initialPreferences = runBlocking { dataStore.data.first() }

    @Volatile
    private var onboardingCompletedCache =
        initialPreferences[PreferenceKeys.ONBOARDING] ?: false

    @Volatile
    private var currentPlayableCache =
        initialPreferences[PreferenceKeys.CURRENT_PLAYABLE].asPlayable()

    @Volatile
    private var linkedServicesCache =
        initialPreferences[PreferenceKeys.LINKED_SERVICES].asLinkedServiceList()

    @Volatile
    private var refreshTokenCache =
        initialPreferences[PreferenceKeys.REFESH_TOKEN] ?: ""

    @Volatile
    private var albumsOnDeviceCache =
        initialPreferences[PreferenceKeys.ALBUMS_ON_DEVICE] ?: true

    @Volatile
    private var songsOnDeviceCache =
        initialPreferences[PreferenceKeys.SONGS_ON_DEVICE] ?: true

    private val datastore: Flow<Preferences>
        get() = dataStore.data

    // Preferences Implementation

    override val requestedPermissionsList: DataStorePreference<List<String>>
        get() = object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> {
                return datastore.map { prefs ->
                    prefs[PreferenceKeys.PERMISSIONS_REQUESTED].asStringList()
                }
            }

            override suspend fun set(value: List<String>) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.PERMISSIONS_REQUESTED] = json.encodeToString(value)
                }
            }
        }


    override val themePreference: DataStorePreference<UiMode>
        get() = object : DataStorePreference<UiMode> {
            override fun getFlow(): Flow<UiMode> =
                datastore.map { it[PreferenceKeys.THEME].asUiMode() }

            override suspend fun set(value: UiMode) {
                dataStore.edit { it[PreferenceKeys.THEME] = value.name }
            }
        }


    override val listeningWhitelist: DataStorePreference<List<String>>
        get() = object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.LISTENING_WHITELIST].asStringList()
                }

            override suspend fun set(value: List<String>) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LISTENING_WHITELIST] = json.encodeToString(value)
                }
            }

            override suspend fun getAndUpdate(update: (List<String>) -> List<String>) {
                dataStore.updateData {
                    val updatedValue = update(it[PreferenceKeys.LISTENING_WHITELIST].asStringList())
                    val mutablePrefs = it.toMutablePreferences()
                    mutablePrefs[PreferenceKeys.LISTENING_WHITELIST] = json.encodeToString(updatedValue)
                    return@updateData mutablePrefs
                }
            }
        }

    override val isNotificationServiceAllowed: Boolean
        get() {
            val listeners =
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(context.packageName)
        }

    override val isListeningAllowed: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.IS_LISTENING_ALLOWED] ?: true
                }

            override suspend fun set(value: Boolean) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.IS_LISTENING_ALLOWED] = value
                }
            }
        }

    override val shouldListenNewPlayers: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS] ?: false
                }

            override suspend fun set(value: Boolean) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS] = value
                }
            }
        }

    override val listeningApps: DataStorePreference<List<String>>
        get() = object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.LISTENING_APPS].asStringList()
                }

            override suspend fun set(value: List<String>) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LISTENING_APPS] = json.encodeToString(value)
                }
            }

            override suspend fun getAndUpdate(update: (List<String>) -> List<String>) {
                dataStore.updateData {
                    val updatedValue = update(it[PreferenceKeys.LISTENING_APPS].asStringList())
                    val mutablePrefs = it.toMutablePreferences()
                    mutablePrefs[PreferenceKeys.LISTENING_APPS] = json.encodeToString(updatedValue)
                    return@updateData mutablePrefs
                }
            }
        }

    override val version: String
        get() = try {
            context.packageManager?.getPackageInfo(context.packageName, 0)!!.versionName ?: "N/A"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }

    override var onboardingCompleted: Boolean
        get() = onboardingCompletedCache
        set(value) {
            onboardingCompletedCache = value
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.ONBOARDING] = value
                }
            }
        }

    override suspend fun logoutUser(): Boolean = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.REFESH_TOKEN)
            prefs.remove(PreferenceKeys.USERNAME)
        }
        refreshTokenCache = ""
        lbAccessToken.set("")
        return@withContext true
    }

    override var currentPlayable: Playable?
        get() = currentPlayableCache
        set(value) {
            value?.let {
                currentPlayableCache = it
                val jsonString = TypeConverter.playableToJSON(it)
                scope.launch {
                    dataStore.edit { prefs ->
                        prefs[PreferenceKeys.CURRENT_PLAYABLE] = jsonString
                    }
                }
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

    override suspend fun isUserLoggedIn(): Boolean =
        lbAccessToken.get().isNotEmpty()

    override val lbAccessToken: DataStorePreference<String>
        get() = object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
                }

            override suspend fun set(value: String) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LB_ACCESS_TOKEN] = value
                }
            }

        }

    override val username: DataStorePreference<String>
        get() = object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.USERNAME] ?: ""
                }

            override suspend fun set(value: String) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.USERNAME] = value ?: ""
                }
            }

        }

    override val consentScreenDataCache: DataStorePreference<String>
        get() = object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> {
                return datastore.map { prefs ->
                    prefs[PreferenceKeys.CONSENT_SCREEN_CACHE] ?: ""
                }
            }

            override suspend fun set(value: String) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.CONSENT_SCREEN_CACHE] = value
                }
            }
        }

    override var linkedServices: List<LinkedService>
        get() = linkedServicesCache
        set(value) {
            linkedServicesCache = value
            val jsonString = json.encodeToString(value)
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LINKED_SERVICES] = jsonString
                }
            }
        }

    override val refreshToken: String?
        get() = refreshTokenCache

    /* BrainzPlayer Preferences */

    override var albumsOnDevice: Boolean
        get() = albumsOnDeviceCache
        set(value) {
            albumsOnDeviceCache = value
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.ALBUMS_ON_DEVICE] = value
                }
            }
        }

    override var songsOnDevice: Boolean
        get() = songsOnDeviceCache
        set(value) {
            songsOnDeviceCache = value
            scope.launch {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.SONGS_ON_DEVICE] = value
                }
            }
        }

    override val installSource: DataStorePreference<InstallSource>
        get() = object : DataStorePreference<InstallSource> {
            override fun getFlow(): Flow<InstallSource> =
                datastore.map { prefs ->
                    val sourceString = prefs[PreferenceKeys.INSTALL_SOURCE]?.toString() ?: ""
                    try {
                        if (sourceString.isNotEmpty()) {
                            InstallSource.valueOf(sourceString)
                        } else {
                            InstallSource.NOT_CHECKED
                        }
                    } catch (e: Exception) {
                        InstallSource.NOT_CHECKED
                    }
                }

            override suspend fun set(value: InstallSource) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.INSTALL_SOURCE] = value.name
                }
            }
        }

    override val appLaunchCount: DataStorePreference<Int>
        get() = object : DataStorePreference<Int> {
            override fun getFlow(): Flow<Int> =
                datastore.map { prefs ->
                    try {
                        prefs[PreferenceKeys.APP_LAUNCH_COUNT]?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                }

            override suspend fun set(value: Int) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.APP_LAUNCH_COUNT] = value.toString()
                }
            }

            override suspend fun getAndUpdate(update: (Int) -> Int) {
                dataStore.updateData {
                    val currentValue = try {
                        it[PreferenceKeys.APP_LAUNCH_COUNT]?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val updatedValue = update(currentValue)
                    val mutablePrefs = it.toMutablePreferences()
                    mutablePrefs[PreferenceKeys.APP_LAUNCH_COUNT] = updatedValue.toString()
                    return@updateData mutablePrefs
                }
            }
        }

    override val lastVersionCheckLaunchCount: DataStorePreference<Int>
        get() = object : DataStorePreference<Int> {
            override fun getFlow(): Flow<Int> =
                datastore.map { prefs ->
                    try {
                        prefs[PreferenceKeys.LAST_VERSION_CHECK_LAUNCH_COUNT]?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                }

            override suspend fun set(value: Int) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LAST_VERSION_CHECK_LAUNCH_COUNT] = value.toString()
                }
            }
        }

    override val lastUpdatePromptLaunchCount: DataStorePreference<Int>
        get() = object : DataStorePreference<Int> {
            override fun getFlow(): Flow<Int> =
                datastore.map { prefs ->
                    try {
                        prefs[PreferenceKeys.LAST_UPDATE_PROMPT_LAUNCH_COUNT]?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                }

            override suspend fun set(value: Int) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LAST_UPDATE_PROMPT_LAUNCH_COUNT] = value.toString()
                }
            }
        }

    override val downloadId: DataStorePreference<Long>
        get() = object : DataStorePreference<Long> {
            override fun getFlow(): Flow<Long> {
                return datastore.map { prefs ->
                    try {
                        prefs[PreferenceKeys.GITHUB_DOWNLOAD_ID]?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
            }

            override suspend fun set(value: Long) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.GITHUB_DOWNLOAD_ID] = value
                }
            }
        }
}
