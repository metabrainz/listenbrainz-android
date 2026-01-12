package org.listenbrainz.shared.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * A type-safe wrapper for DataStore preferences.
 * This interface can be used in common code across all platforms.
 */
interface DataStorePreference<T> {
    /**
     * Get the current value of the preference.
     */
    suspend fun get(): T = getFlow().first()

    /**
     * Get a Flow that emits the preference value whenever it changes.
     */
    fun getFlow(): Flow<T>

    /**
     * Set the preference value.
     */
    suspend fun set(value: T)

    /**
     * Update the value of the preference in atomic read-modify-write manner.
     */
    suspend fun getAndUpdate(update: (T) -> T): Unit =
        throw NotImplementedError("getAndUpdate has not been implemented for this preference.")
}
