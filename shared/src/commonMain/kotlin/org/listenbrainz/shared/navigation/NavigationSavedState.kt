package org.listenbrainz.shared.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** Saves back stacks of [NavigationItem]s without reflection, so it works on every platform. */
@OptIn(ExperimentalSerializationApi::class)
val navigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<NavigationItem>()
        }
    }
}
