package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.datastore.Preference
import org.listenbrainz.android.util.datastore.Preference.Companion.ComplexPreference
import org.listenbrainz.android.util.datastore.Preference.Companion.PrimitivePreference

object MockPreferences {
    fun <T> mockPrimitivePreference(mockValue: T): PrimitivePreference<T> =
        object : PrimitivePreference<T> {
            override suspend fun get(): T = mockValue
            
            override fun getFlow(): Flow<T> = flow { emit(mockValue) }
            
            override suspend fun getAndUpdate(update: (T) -> T) {}
            
            override suspend fun set(value: T): Resource<T> = Resource.success(mockValue)
        }
    
    fun <T> mockComplexPreference(mockValue: T) =
        object : ComplexPreference<T> {
            override suspend fun get(): T = mockValue
            
            override fun getFlow(): Flow<T> = flow { emit(mockValue) }
            
            override suspend fun getAndUpdate(update: (T) -> T) {}
            
            override suspend fun set(value: T): Resource<T> = Resource.success(mockValue)
        }
    
    fun <T, R> mockPreference(mockValue: T): Preference<T, R> =
        object: Preference<T, R> {
            override suspend fun get(): T = mockValue
            
            override fun getFlow(): Flow<T> = flow { emit(mockValue) }
            
            override suspend fun getAndUpdate(update: (T) -> T) {}
            
            override suspend fun set(value: T): Resource<T> = Resource.success(mockValue)
        }
}