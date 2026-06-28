package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Bitmap
import coil3.request.ImageRequest
import coil3.request.allowHardware
import org.listenbrainz.shared.applicationContext

actual val coilPlatformContext: coil3.PlatformContext get() = applicationContext

actual fun ImageRequest.Builder.platformConfig(): ImageRequest.Builder = this.allowHardware(false)

actual fun Bitmap.convertToImageBitmap(): ImageBitmap = this.asImageBitmap()