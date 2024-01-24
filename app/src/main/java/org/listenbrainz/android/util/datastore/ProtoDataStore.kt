package org.listenbrainz.android.util.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource
import java.io.IOException

abstract class ProtoDataStore(private val dataStore: DataStore<Preferences>) {
    private fun <T> defaultSerializer(defaultValue: T): DataStoreSerializer<T, T> =
        object: DataStoreSerializer<T, T> {
            override fun from(value: T): T = value
            override fun to(value: T): T = value
            override fun default(): T = defaultValue
        }
    
    /** [DataStorePreference]s which are primitive in nature can use this class.*/
    abstract inner class PrimitiveDataStorePreference<T>(
        key: Preferences.Key<T>,
        defaultValue: T
    ): DataStorePreference<T, T>(key, defaultSerializer(defaultValue))
    
    
    /** A [DataStore] preference can be declared type-safe by making an object of this class.
     *
     * Every function can be overridden.*/
    abstract inner class DataStorePreference<T, R>(
        private val key: Preferences.Key<R>,
        private val serializer: DataStoreSerializer<T, R>
    ) {
        open suspend fun get(): T = getFlow().firstOrNull() ?: serializer.default()
        
        open fun getFlow(): Flow<T> =
            dataStore.data.map { prefs ->
                prefs[key]?.let { serializer.from(it) } ?: serializer.default()
            }.catch {
                if (it is IOException)
                    emit(serializer.default())
                else
                    throw it
            }
        
        /** @return [Resource] If the value was updated or not.*/
        open suspend fun set(value: T): Resource<T> =
            try {
                dataStore.edit { it[key] = serializer.to(value) }
                Resource.success(value)
            } catch (e: IOException) {
                Resource.failure(error = ResponseError.FILE_NOT_FOUND.apply { actualResponse = e.localizedMessage })
            }
        
        /** Update the value of the preference in an atomic read-modify-write manner.*/
        open suspend fun getAndUpdate(update: (T) -> T) =
            dataStore.updateData { prefs ->
                val mutablePrefs = prefs.toMutablePreferences()
                val currentValue = prefs[key]?.let { serializer.from(it) } ?: serializer.default()
                mutablePrefs[key] = serializer.to(update(currentValue))
                return@updateData mutablePrefs
            }
    }
}