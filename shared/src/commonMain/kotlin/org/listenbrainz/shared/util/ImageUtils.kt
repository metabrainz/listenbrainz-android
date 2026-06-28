package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Bitmap
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap

suspend fun fetchBitmapFromUrl(url: String): ImageBitmap? {
    return try {
        val request = ImageRequest.Builder(coilPlatformContext)
            .data(url)
            .platformConfig()
            .build()

        val result = SingletonImageLoader.get(coilPlatformContext).execute(request)
        if (result is SuccessResult) {
            result.image.toBitmap().convertToImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("Error while fetching bitmap: ${e.message}")
        null
    }
}

expect val coilPlatformContext: coil3.PlatformContext
expect fun ImageRequest.Builder.platformConfig(): ImageRequest.Builder
expect fun Bitmap.convertToImageBitmap(): ImageBitmap