package org.listenbrainz.android.util

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import org.listenbrainz.android.R
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.ResponseError.Companion.getError
import org.listenbrainz.android.util.Constants.Strings.CHANNEL_ID
import org.listenbrainz.android.util.Log.e
import retrofit2.Response
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * A set of fairly general Android utility methods.
 */
object Utils {
    
    /** General function to parse an API endpoint's response.
     * @param request Call the API endpoint here. Run any pre-conditional checks to directly return error/success in some cases. */
    inline fun <T> parseResponse(request: () -> Response<T>): Resource<T> =
        runCatching<Resource<T>> {
            val response = request()
            
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                val error = getError(response = response)
                Resource.failure(error = error)
            }
        
        }.getOrElse { logAndReturn(it) }
    
    fun <T> logAndReturn(it: Throwable) : Resource<T> {
        it.printStackTrace()
        return when (it){
            is FileNotFoundException -> Resource.failure(error = ResponseError.FILE_NOT_FOUND)
            is IOException -> Resource.failure(error = ResponseError.NETWORK_ERROR)
            else -> Resource.failure(error = ResponseError.UNKNOWN)
        }
    }
    
    /** Get *CoverArtArchive* url for cover art of a release.
     * @param size Allowed sizes are 250, 500, 750 and 1000. Default is 250.*/
    fun getCoverArtUrl(caaReleaseMbid: String?, caaId: Long?, size: Int = 250): String? {
        if (caaReleaseMbid == null || caaId == null) return null
        return "https://archive.org/download/mbid-${caaReleaseMbid}/mbid-${caaReleaseMbid}-${caaId}_thumb${size}.jpg"
    }
    
    fun similarityToPercent(similarity: Float?): String {
        return if (similarity != null)
            "${(similarity * 100).toInt()}%"
        else
            ""
    }
    
    fun Context.getActivity(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
    

    /** Get human readable error.
     *
     * **CAUTION:** If this function is called once, calling it further with the same [Response] instance will result in an empty
     * string. Store this function's result for multiple use cases.*/
    fun <T> Response<T>.error(): String? = this.errorBody()?.string()
    
    fun sendFeedback(context: Context) {
        try {
            context.startActivity(emailIntent(Constants.FEEDBACK_EMAIL, Constants.FEEDBACK_SUBJECT))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.toast_feedback_fail, Toast.LENGTH_LONG).show()
        }
    }
    
    fun getArticle(str: String): String {
        return when (str.first()){
            'a' -> "an"
            'e' -> "an"
            'i' -> "an"
            'o' -> "an"
            'u' -> "an"
            else -> "a"
        }
    }

    fun emailIntent(recipient: String, subject: String?): Intent {
        val uri = Uri.parse("mailto:$recipient")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        return intent
    }

    fun getSHA1(context: Context, packageName: String): String? {
        try {
            val signatures = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                return md.digest().joinToString("") { "%02X".format(it) }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    fun Context.openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }
    
    
    fun notifyListen(songTitle: String, artistName: String, albumArt: Bitmap?, nm: NotificationManager, context: Context) {
        val notificationBuilder = NotificationCompat.Builder(context,
            CHANNEL_ID
        )
        .setContentTitle(songTitle)
        .setContentText(artistName)
        .setSmallIcon(R.drawable.ic_listenbrainz_logo_no_text)
        .setLargeIcon(albumArt) // Set the album art here
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setAutoCancel(false)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            nm.notify(0, notificationBuilder.build())
        } catch (e: RuntimeException) {
            e(message = "Error showing notification")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun Icon.toBitmap(context: Context): Bitmap? {
        val drawable = this.loadDrawable(context)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = drawable?.let {
                Bitmap.createBitmap(
                    it.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            }
            val canvas = bitmap?.let { Canvas(it) }
            if (canvas != null) {
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
            bitmap
        }
    }

    fun listenFromNotiExtractMeta(titleStr: String, formatStr: String): Pair<String, String>? {
        val tpos = formatStr.indexOf("%1\$s")
        val apos = formatStr.indexOf("%2\$s")
        val regex = formatStr.replace("(", "\\(")
            .replace(")", "\\)")
            .replace("%1\$s", "(.*)")
            .replace("%2\$s", "(.*)")
        return try {
            val m = regex.toRegex().find(titleStr)!!
            val g = m.groupValues
            if (g.size != 3)
                throw IllegalArgumentException("group size != 3")
            if (tpos > apos)
                g[1] to g[2]
            else
                g[2] to g[1]

        } catch (e: Exception) {
            print("err in $titleStr $formatStr")
            null
        }
    }

    fun stringFromAsset(context: Context, asset: String?): String {
        return try {
            val input = context.resources.assets.open(asset!!)
            val buffer = ByteArray(input.available())
            input.read(buffer)
            val output = ByteArrayOutputStream()
            output.write(buffer)
            output.close()
            input.close()
            output.toString()
        } catch (e: IOException) {
            e(message = "Error reading text file from assets folder.")
            ""
        }
    }

    /** Save a given bitmap to the default images directory inside the "ListenBrainz"
     * directory of the device.
     *
     * RUN ON [Dispatchers.IO]
     *
     * @param mimeType Type of file to be saved. (Types: "image/png", "image/jpg" and "image/jpeg" only)
     * @param quality Percent quality of compression. Default is 95.
     * @param displayName Name of the file to save. Use [System.currentTimeMillis] / 1000 if not sure.
     * @param launchShareIntent Launch share chooser when the bitmap is saved to the device. Default is `false`.
     *
     * @return [String] - Path of the image, inside external storage, that is stored.
     *
     * @exception SecurityException `WRITE_EXTERNAL_STORAGE` should be requested first for sdks below 28.
     * @exception IOException If standard IO operations fail.
     * @exception FileNotFoundException If the device's directory is mounted to computer, share intent stops working as
     * the internal storage is inaccessible to other apps which want the bitmap.
     *
     * @author jasje on IRC*/
    @Throws(IOException::class,IllegalArgumentException::class)
    @WorkerThread
    fun saveBitmap(
        context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat,
        displayName: String, quality: Int = 95, launchShareIntent: Boolean = false
    ): String? {
        
        val mimeType: String = when(format){
            Bitmap.CompressFormat.PNG -> "image/png"
            Bitmap.CompressFormat.JPEG -> "image/jpeg"
            else -> throw IllegalArgumentException("Unsupported CompressFormat type.")
        }
        
        var resultUrl : String? = null
        
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "ListenBrainz")
                //put(MediaStore.MediaColumns.IS_PENDING, 1)
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            }
            
            val resolver = context.contentResolver
            var uri: Uri? = null
            
            try {
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Failed to create new MediaStore record.")
                
                resolver.openOutputStream(uri)?.use {
                    if (!bitmap.compress(format, quality, it))
                        throw IOException("Failed to save bitmap.")
                } ?: throw IOException("Failed to open output stream.")
                
                /*values.clear()
                   values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                   resolver.update(uri, values, null, null)*/
                
                // Getting path from content resolver.
                resolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null).use {
                    val columnIndex = it?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    it?.moveToFirst()
                    resultUrl = columnIndex?.let { cIndex -> it.getString(cIndex) }
                }
                
            } catch (e: Exception) {
                
                uri?.let { orphanUri ->
                    // Clean up orphan entry in the MediaStore.
                    resolver.delete(orphanUri, null, null)
                }
                e.localizedMessage?.let { Log.e("saveBitmap", it ) }
            }
        }else{
            // For sdks below 29
            try {
                
                val folderPath = (Environment.getExternalStorageDirectory().toString() + File.separator
                        + Environment.DIRECTORY_PICTURES + File.separator
                        + "ListenBrainz")
                
                val imagePath = (
                        folderPath + File.separator
                                + displayName
                                + when (mimeType) {
                            "image/png" -> ".png"
                            "image/jpeg" -> ".jpeg"
                            else -> { throw IllegalArgumentException("Unsupported or incorrect mime type.") }
                        }
                        )
                
                val appImagesFolder = File(folderPath)
                
                if (!appImagesFolder.exists()) {
                    if (appImagesFolder.mkdirs())        // Making sure folder exists.
                        e("saveBitmap", "Successfully created app directory.")
                    else
                        e("saveBitmap", "Failed to create a directory.", )
                }
                
                val f = File(imagePath)
                
                FileOutputStream(f).use {
                    if (!bitmap.compress(format, quality, it)) {
                        throw IOException("Failed to save bitmap")
                    }
                }
                
                resultUrl = f.absolutePath
                
            } catch (e: Exception){
                e.localizedMessage?.let { Log.e("saveBitmap", it, ) }
            }
        }
        // Launching share chooser
        if (launchShareIntent) {
            MediaScannerConnection.scanFile(context, arrayOf(resultUrl), null) { _, uri ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mimeType  // This is mime type
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        }
        
        return resultUrl
    }
}