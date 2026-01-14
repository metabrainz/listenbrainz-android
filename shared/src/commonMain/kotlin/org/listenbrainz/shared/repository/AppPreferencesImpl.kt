package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.listenbrainz.shared.model.InstallSource
import org.listenbrainz.shared.model.Playable
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.model.UiMode.Companion.asUiMode
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.preferences.DataStorePreference
import org.listenbrainz.shared.preferences.DATA_STORE_FILE_NAME
import org.listenbrainz.shared.preferences.PreferenceKeys
import org.listenbrainz.shared.preferences.createDataStore

class AppPreferencesImpl(private val context: PlatformContext) : AppPreferences {
    companion object {
        private const val STATUS_LOGGED_IN = 1
        private const val STATUS_LOGGED_OUT = 0
        private const val LEGACY_PERMS_KEY = "perms_code"
        private val json = Json { ignoreUnknownKeys = true }

        private val permsMigration: DataMigration<Preferences> =
            object : DataMigration<Preferences> {
                override suspend fun cleanUp() = Unit

                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    return currentData.contains(stringPreferencesKey(LEGACY_PERMS_KEY))
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    val mutablePreferences = currentData.toMutablePreferences()
                    mutablePreferences.remove(stringPreferencesKey(LEGACY_PERMS_KEY))
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

                    val whitelist =
                        currentData[PreferenceKeys.LISTENING_WHITELIST].asStringList().toMutableSet()
                    appList.forEach { pkg ->
                        if (!blacklist.contains(pkg)) {
                            whitelist.add(pkg)
                        }
                    }

                    val mutablePreferences = currentData.toMutablePreferences()
                    mutablePreferences[PreferenceKeys.LISTENING_WHITELIST] =
                        json.encodeToString(whitelist.toList())
                    mutablePreferences.remove(PreferenceKeys.LISTENING_BLACKLIST)

                    return mutablePreferences.toPreferences()
                }
            }

        private fun baseMigrations(): List<DataMigration<Preferences>> =
            listOf(blacklistMigration, permsMigration)

        @Volatile
        private var sharedDataStore: DataStore<Preferences>? = null

        private fun getSharedDataStore(context: PlatformContext): DataStore<Preferences> {
            val existing = sharedDataStore
            if (existing != null) return existing
            return synchronized(this) {
                val cached = sharedDataStore
                if (cached != null) {
                    cached
                } else {
                    platformInitDataStoreContext(context)
                    val created = createDataStore(
                        name = DATA_STORE_FILE_NAME,
                        migrations = platformDataMigrations(context, baseMigrations())
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
                else json.decodeFromString(this)
            } catch (e: Exception) {
                null
            }
        }
    }

    private val dataStore: DataStore<Preferences> = getSharedDataStore(context)

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
        get() = platformIsNotificationServiceAllowed(context)

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
        get() = platformPackageVersion(context)

    override val onboardingCompleted: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.ONBOARDING] ?: false
                }

            override suspend fun set(value: Boolean) {
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
        lbAccessToken.set("")
        return@withContext true
    }

    override val currentPlayable: DataStorePreference<Playable?>
        get() = object : DataStorePreference<Playable?> {
            override fun getFlow(): Flow<Playable?> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.CURRENT_PLAYABLE].asPlayable()
                }

            override suspend fun set(value: Playable?) {
                dataStore.edit { prefs ->
                    if (value == null) {
                        prefs.remove(PreferenceKeys.CURRENT_PLAYABLE)
                    } else {
                        prefs[PreferenceKeys.CURRENT_PLAYABLE] = json.encodeToString(value)
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
                    prefs[PreferenceKeys.USERNAME] = value
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

    override val linkedServices: DataStorePreference<List<LinkedService>>
        get() = object : DataStorePreference<List<LinkedService>> {
            override fun getFlow(): Flow<List<LinkedService>> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.LINKED_SERVICES].asLinkedServiceList()
                }

            override suspend fun set(value: List<LinkedService>) {
                val jsonString = json.encodeToString(value)
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LINKED_SERVICES] = jsonString
                }
            }
        }

    override val refreshToken: DataStorePreference<String?>
        get() = object : DataStorePreference<String?> {
            override fun getFlow(): Flow<String?> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.REFESH_TOKEN] ?: ""
                }

            override suspend fun set(value: String?) {
                dataStore.edit { prefs ->
                    if (value == null) {
                        prefs.remove(PreferenceKeys.REFESH_TOKEN)
                    } else {
                        prefs[PreferenceKeys.REFESH_TOKEN] = value
                    }
                }
            }
        }

    /* BrainzPlayer Preferences */
    override val albumsOnDevice: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.ALBUMS_ON_DEVICE] ?: true
                }

            override suspend fun set(value: Boolean) {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.ALBUMS_ON_DEVICE] = value
                }
            }
        }

    override val songsOnDevice: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.SONGS_ON_DEVICE] ?: true
                }

            override suspend fun set(value: Boolean) {
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
