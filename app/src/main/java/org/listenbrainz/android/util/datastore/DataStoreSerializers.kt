package org.listenbrainz.android.util.datastore

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.model.UiMode.Companion.asUiMode
import org.listenbrainz.android.util.LinkedService

object DataStoreSerializers {
    private val gson = Gson()
    /** Serializes to String primitive.*/
    private fun <T> gsonSerializer(defaultValue: T): DataStoreSerializer<T, String> =
        object: DataStoreSerializer<T, String> {
            override fun from(value: String): T =
                gson.fromJson(
                    value,
                    object: TypeToken<T>() {}.type
                ) ?: defaultValue
            
            override fun to(value: T): String = gson.toJson(value)
            
            override fun default(): T = defaultValue
        }
    
    val themeSerializer: DataStoreSerializer<UiMode, String>
        get() = object: DataStoreSerializer<UiMode, String> {
            override fun from(value: String): UiMode = value.asUiMode()
            
            override fun to(value: UiMode): String = value.name
            
            override fun default(): UiMode = UiMode.FOLLOW_SYSTEM
        }
    
    
    val stringListSerializer: DataStoreSerializer<List<String>, String>
        get() = gsonSerializer(emptyList())
    
    val linkedServicesListSerializer: DataStoreSerializer<List<LinkedService>, String>
        get() = gsonSerializer(emptyList())
}