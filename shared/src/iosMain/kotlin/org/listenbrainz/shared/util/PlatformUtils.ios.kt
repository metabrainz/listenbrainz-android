package org.listenbrainz.shared.util

import org.listenbrainz.shared.repository.PlatformContext

actual object PlatformUtils{
    actual fun getSHA1(context: PlatformContext,packageName:String):String?{
        return null
    }
}