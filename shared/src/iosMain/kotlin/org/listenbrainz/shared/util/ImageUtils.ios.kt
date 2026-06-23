package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import org.listenbrainz.shared.repository.PlatformContext

actual suspend fun fetchBitmapFromUrl(context: PlatformContext, url: String): ImageBitmap? {
    return try {
        val request = ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
            .data(url)
            .build()

        val result = SingletonImageLoader.get(coil3.PlatformContext.INSTANCE).execute(request)
        if(result is SuccessResult){
            result.image.toBitmap().asComposeImageBitmap()
        } else{
            null
        }
    } catch (e: Exception){
        Log.e("Error while fetching bitmap: ${e.message}")
        null
    }
}