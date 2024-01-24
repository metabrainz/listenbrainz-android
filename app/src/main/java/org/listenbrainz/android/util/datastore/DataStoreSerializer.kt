package org.listenbrainz.android.util.datastore

/** @property T Object or higher type.
 *  @property R Primitive or serializable type.*/
interface DataStoreSerializer<T, R> {
    /** Convert from Primitive [R] to Object [T].*/
    fun from(value: R): T
    
    /** Convert to Primitive [T] to Object [R].*/
    fun to(value: T): R
    
    /** Default value for errors and null values.*/
    fun default(): T
}