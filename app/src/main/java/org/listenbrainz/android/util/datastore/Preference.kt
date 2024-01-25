package org.listenbrainz.android.util.datastore

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.util.Resource

/** A Type-safe preference.*/
interface Preference <T, R> {
    suspend fun get(): T
    
    fun getFlow(): Flow<T>
    
    /** @return [Resource] If the value was updated or not.*/
    suspend fun set(value: T): Resource<T>
    
    /** Update the value of the preference in an atomic read-modify-write manner.*/
    suspend fun getAndUpdate(update: (T) -> T)
    
    companion object {
        /** [Preference]s which are primitive in nature can implement this interface.*/
        interface PrimitivePreference<T>: Preference<T, T>
        
        /** [Preference]s which have to be converted to [String] when saved can implement this interface.*/
        interface ComplexPreference<T>: Preference<T, String>
    }
}

