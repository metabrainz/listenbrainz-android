package org.listenbrainz.android.util

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import org.listenbrainz.android.util.Log.e
import java.io.*
import java.util.*

/**
 * A set of fairly general Android utility methods.
 */
object Utils {
    
    /** Get *CoverArtArchive* url for cover art of a release.
     * @param size Allowed sizes are 250, 500, 750 and 1000. Default is 250.*/
    fun getCoverArtUrl(caaReleaseMbid: String?, caaId: Long?, size: Int = 250): String {
        return  "https://archive.org/download/mbid-${caaReleaseMbid}/mbid-${caaReleaseMbid}-${caaId}_thumb${size}.jpg"
    }

    fun shareIntent(text: String?): Intent {
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain")
        return intent.putExtra(Intent.EXTRA_TEXT, text)
    }

    fun emailIntent(recipient: String, subject: String?): Intent {
        val uri = Uri.parse("mailto:$recipient")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        return intent
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
            e("Error reading text file from assets folder.")
            ""
        }
    }

    fun changeLanguage(context: Context, lang_code: String): ContextWrapper {
        var context = context
        val sysLocale: Locale
        val rs = context.resources
        val config = rs.configuration
        sysLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            config.locale
        }
        if (lang_code != "" && sysLocale.language != lang_code) {
            val locale = Locale(lang_code)
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                config.locale = locale
            }
            context = context.createConfigurationContext(config)
        }
        return ContextWrapper(context)
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
                        Log.e("saveBitmap", "Failed to create a directory.", )
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