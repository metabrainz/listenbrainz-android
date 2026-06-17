package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import org.listenbrainz.shared.repository.PlatformContext

expect suspend fun fetchBitmapFromUrl(context: PlatformContext,url:String): ImageBitmap?
