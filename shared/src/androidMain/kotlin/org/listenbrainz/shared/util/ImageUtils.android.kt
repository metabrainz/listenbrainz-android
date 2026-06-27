package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Bitmap
import coil3.request.ImageRequest
import coil3.request.allowHardware
import org.listenbrainz.shared.preferences.AndroidDataStoreContext

actual fun getCoilPlatformContext(): coil3.PlatformContext = AndroidDataStoreContext.require()

actual fun ImageRequest.Builder.platformConfig(): ImageRequest.Builder = this.allowHardware(false)

actual fun Bitmap.convertToImageBitmap(): ImageBitmap = this.asImageBitmap()