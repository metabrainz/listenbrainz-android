package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.flow.update
import org.listenbrainz.shared.repository.PlatformContext

actual suspend fun fetchBitmapFromUrl(context: PlatformContext, url:String): ImageBitmap? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = SingletonImageLoader.get(context).execute(request)
        if (result is SuccessResult) {
            result.image.toBitmap().asImageBitmap()
        }
        else{
            null
        }
    }catch (e: Exception){
//        Log.e("Error while fetching bitmap: ${e.message}")
        null
    }
}
