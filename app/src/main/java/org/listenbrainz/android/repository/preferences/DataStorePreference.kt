package org.listenbrainz.android.repository.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface DataStorePreference<T> {
    suspend fun get(): T = getFlow().first()
    
    fun getFlow(): Flow<T>
    
    suspend fun set(value: T)
}