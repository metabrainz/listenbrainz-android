package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.Bitmap
import coil3.request.ImageRequest

actual fun getCoilPlatformContext(): coil3.PlatformContext = coil3.PlatformContext.INSTANCE

actual fun ImageRequest.Builder.platformConfig(): ImageRequest.Builder = this

actual fun Bitmap.convertToImageBitmap(): ImageBitmap = this.asComposeImageBitmap()
