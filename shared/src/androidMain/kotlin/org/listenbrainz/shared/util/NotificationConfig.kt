package org.listenbrainz.shared.util

import kotlin.reflect.KClass

object NotificationConfig {
    var targetActivityClass: Class<*>? =null

    fun initialize(activityClass: KClass<*>){
        this.targetActivityClass = activityClass.java
    }
}