package org.listenbrainz.android.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.listenbrainz.android.App
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntityType
import org.listenbrainz.android.presentation.UserPreferences
import org.listenbrainz.android.presentation.features.adapters.ResultItem
import org.listenbrainz.android.presentation.features.adapters.ResultItemUtils
import org.listenbrainz.android.util.Log.e
import java.io.*
import java.net.URLEncoder
import java.util.*

/**
 * A set of fairly general Android utility methods.
 */
object Utils {

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

    fun toResultItemsList(entity: MBEntityType, response: Resource<String>): Resource<List<ResultItem>> {
        return try {
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    val resultItems = ResultItemUtils.getJSONResponseAsResultItemList(response.data, entity)
                    return Resource(Resource.Status.SUCCESS, resultItems)
                }
                else -> Resource(Resource.Status.FAILED, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(Resource.Status.FAILED, null)
        }
    }
    
    /** Save a given bitmap to the default images directory inside the "ListenBrainz"
     * directory of the device.
     *
     * @param mimeType Type of file to be saved. (Types: "image/png", "image/jpg" and "image/jpeg" only)
     * @param quality Percent quality of compression. Default is 95.
     * @param displayName Name of the file to save. Use [System.currentTimeMillis] / 1000 if not sure.
     * @param launchShareIntent Launch share chooser when the bitmap is saved to the device. Default is `false`.
     *
     * @return [Uri] of table row given by content resolver if `Build.VERSION.SDK_INT >= 29` else the uri of image that is stored.
     *
     * @exception SecurityException `WRITE_EXTERNAL_STORAGE` should be requested first for sdks below 28.
     * @exception IOException If standard IO operations fail.
     * @exception FileNotFoundException If the device's directory is mounted to computer, share intent stops working as
     * the internal storage is inaccessible to other apps which want the bitmap.
     *
     * @author jasje on IRC*/
    @Throws(IOException::class)
    fun saveBitmap(
        context: Context, scope: CoroutineScope, bitmap: Bitmap, format: Bitmap.CompressFormat,
        mimeType: String, displayName: String, quality: Int = 95, launchShareIntent: Boolean = false
    ): Uri? {
        val folderPath = (Environment.getExternalStorageDirectory().toString() + File.separator
                + Environment.DIRECTORY_PICTURES + File.separator
                + "ListenBrainz")
        
        val imagePath = (
                folderPath + File.separator
                        + displayName
                        + when (mimeType) {
                            "image/png" -> ".png"
                            "image/jpg" -> ".jpg"
                            "image/jpeg" -> ".jpeg"
                            else -> { throw IllegalArgumentException("Unsupported or incorrect mime type.") }
                        }
                )
        
        var resultUri : Uri? = null
        scope.launch(Dispatchers.IO) {
            
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
                    resultUri = uri
                    
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
                    
                    resultUri = f.toUri()
                    
                } catch (e: Exception){
                    e.localizedMessage?.let { Log.e("saveBitmap", it, ) }
                }
            }
            // Launching share chooser
            if (launchShareIntent) {
                MediaScannerConnection.scanFile(context, arrayOf(imagePath), null) { _, uri ->
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = mimeType  // This is mime type
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share your Year in Music"))     // TODO: Make this flexible
                }
            }
            
        }
        
        return resultUri
        
    }
}