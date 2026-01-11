package org.listenbrainz.android.util

import android.Manifest
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration.ORIENTATION_PORTRAIT
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.limurse.logger.Logger
import com.limurse.logger.util.FileIntent
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.R
import org.listenbrainz.android.model.ApiError
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.ResponseError.AUTH_HEADER_NOT_FOUND
import org.listenbrainz.android.model.ResponseError.BAD_GATEWAY
import org.listenbrainz.android.model.ResponseError.BAD_REQUEST
import org.listenbrainz.android.model.ResponseError.DOES_NOT_EXIST
import org.listenbrainz.android.model.ResponseError.INTERNAL_SERVER_ERROR
import org.listenbrainz.android.model.ResponseError.RATE_LIMIT_EXCEEDED
import org.listenbrainz.android.model.ResponseError.SERVICE_UNAVAILABLE
import org.listenbrainz.android.model.ResponseError.UNAUTHORISED
import org.listenbrainz.android.model.ResponseError.UNKNOWN
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * A set of fairly general Android utility methods.
 */
object Utils {
    
    class PreEmptiveBadRequestException(val responseError: ResponseError) : Exception()

    class KtorRequestScope {
        @OptIn(ExperimentalContracts::class)
        fun failIf(condition: Boolean, error: () -> ResponseError) {
            contract {
                returns() implies !condition
            }
            if (condition) {
                throw PreEmptiveBadRequestException(error())
            }
        }
    }

    /** General function to parse an API endpoint's response executed by Ktor.
     * @param request Call the API endpoint here. Run any pre-conditional checks to directly return error/success in some cases. */
    suspend inline fun <T> parseResponse(request: KtorRequestScope.() -> T): Resource<T> =
        runCatching {
            Resource.success(KtorRequestScope().request())
        }.getOrElse { error ->
            return@getOrElse when (error) {
                is ResponseException -> {
                    val code = error.response.status
                    val responseError = when (code) {
                        HttpStatusCode.BadRequest -> BAD_REQUEST
                        HttpStatusCode.Unauthorized -> AUTH_HEADER_NOT_FOUND
                        HttpStatusCode.Forbidden -> UNAUTHORISED
                        HttpStatusCode.NotFound -> DOES_NOT_EXIST
                        HttpStatusCode.TooManyRequests -> RATE_LIMIT_EXCEEDED
                        HttpStatusCode.InternalServerError -> INTERNAL_SERVER_ERROR
                        HttpStatusCode.BadGateway -> BAD_GATEWAY
                        HttpStatusCode.ServiceUnavailable -> SERVICE_UNAVAILABLE
                        else -> UNKNOWN
                    }.apply {
                        actualResponse = error.response.body<ApiError>().error
                    }

                    Resource.failure(responseError)
                }
                is PreEmptiveBadRequestException -> Resource.failure(error.responseError)
                else -> logAndReturn(error)
            }
        }
    
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

    fun Context.showToast(
        message: String,
        length: Int = Toast.LENGTH_LONG
    ) = Toast.makeText(this, message, length).show()
    

    fun List<Placeable>.measureSize(): IntSize {
        var width = 0
        var height = 0
        forEach {
            width += it.width
            height += it.height
        }

        return IntSize(width, height)
    }

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

    @Composable
    fun LaunchedEffectUnit(block: suspend CoroutineScope.() -> Unit) = LaunchedEffect(Unit, block)

    @Composable
    fun LaunchedEffectUnitMainThread(block: () -> Unit) = DisposableEffect(Unit) {
        block()
        EmptyDisposableEffectResult
    }

    @Composable
    fun LaunchedEffectMainThread(vararg keys: Any?, block: () -> Unit) = DisposableEffect(*keys) {
        block()
        EmptyDisposableEffectResult
    }

    @Composable
    fun LaunchedEffectMainThread(key1: Any?, block: () -> Unit) = DisposableEffect(key1) {
        block()
        EmptyDisposableEffectResult
    }

    private val EmptyDisposableEffectResult = object : DisposableEffectResult {
        override fun dispose() = Unit
    }

    fun Context.getNavigationBarHeightInPixels(): Int {
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /** Works for cases where the status bar may change, i.e., foldables.*/
    fun Context.getStatusBarHeightInPixels(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    @Composable
    fun getStatusBarHeight() =
        WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()

    @Composable
    fun getDisplayCutoutHeight() =
        if (LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT) {
            WindowInsets.displayCutout.asPaddingValues()
                .calculateTopPadding()
        } else {
            WindowInsets.displayCutout.asPaddingValues()
                .calculateStartPadding(LocalLayoutDirection.current)
        }

    @Composable
    fun getNavigationBarHeight() =
        WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()

    @Composable
    fun HorizontalSpacer(width: Dp) = Spacer(Modifier.width(width))

    @Composable
    fun VerticalSpacer(height: Dp) = Spacer(Modifier.height(height))

    @Composable
    fun ColumnScope.Spacer(height: Dp) = Spacer(modifier = Modifier.height(height))

    @Composable
    fun RowScope.Spacer(height: Dp) = Spacer(modifier = Modifier.width(height))

    @Composable
    fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }
    @Composable
    fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

    @Composable
    fun Int.toSp() = with(LocalDensity.current) { this@toSp.toSp() }
    @Composable
    fun Float.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

    @Composable
    fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

    fun Number.toDp(context: Context) = (this.toFloat() / context.resources.displayMetrics.density).dp
    fun Number.toDp(density: Int) = (this.toFloat() / density).dp

    fun Dp.toPx(context: Context) = this.value * context.resources.displayMetrics.density
    fun Dp.toPx(density: Int) = this.value * density

    @Composable
    fun TextUnit.toDp() = with(LocalDensity.current) { this@toDp.toDp() }
    fun TextUnit.toDp(context: Context) {
        val density = Density(context)
        return with(density) { this@toDp.toDp() }
    }

    @Composable
    fun SetSystemBarsForegroundAppearance(lightAppearance: Boolean) {
        val view = LocalView.current

        if (!view.isInEditMode) {
            val activity = view.context.getActivity() ?: return
            SideEffect {
                WindowCompat.getInsetsController(activity.window, view).run {
                    // turn left to go right
                    isAppearanceLightStatusBars = !lightAppearance
                    isAppearanceLightNavigationBars = !lightAppearance
                }
            }
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
            val signatures = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures ?: emptyArray()
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

    fun Context.getListeningApps(): List<ApplicationInfo>{
        val intents = listOf(
            "android.media.browse.MediaBrowserService",
            "android.media.session.MediaSessionService"
        )
        val musicAppsPackages = mutableListOf<ApplicationInfo>()
        intents.forEach { intentString->
            val services = packageManager.queryIntentServices(Intent(intentString), PackageManager.GET_META_DATA)
            for (resolveInfo in services){
                val packageName = resolveInfo.serviceInfo.packageName
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val category = packageManager.getApplicationInfo(packageName, 0).category
                    if (category == ApplicationInfo.CATEGORY_AUDIO || category == ApplicationInfo.CATEGORY_VIDEO){
                        musicAppsPackages.add(appInfo)
                    }
                }else{
                    //Don't apply the additional category filter for API version < 26
                    musicAppsPackages.add(appInfo)
                }
            }
        }
        return musicAppsPackages
    }

    //Function to fetch installed apps
    fun Context.getAllInstalledApps(): List<ApplicationInfo>{
        val apps = mutableListOf<ApplicationInfo>()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        packageManager.queryIntentActivities(intent, 0).forEach { resolveInfo->
            try {
                val appInfo = packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, 0)
                apps.add(appInfo)
            }catch (e: Exception){
                Log.e("Could not fetch ApplicationInfo for ${resolveInfo.activityInfo.packageName}")
            }
        }
        return apps
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
            Log.e(message = "Error reading text file from assets folder.")
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
                        Log.e("saveBitmap", "Successfully created app directory.")
                    else
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

    // Function to remove HTML tags from a string
    fun removeHtmlTags(input: String): String {
        // Regular expression pattern to match HTML tags
        val regex = "<[^>]*>".toRegex()
        // Replace all matches of the pattern with an empty string
        return input.replace(regex, "")
    }

    // Function to share a link
    fun shareLink(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        context.startActivity(Intent.createChooser(intent, "Share link via"))
    }

    //Format duration in seconds to HH:MM:SS format or MM:SS format
    fun formatDurationSeconds(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    //Checking if a string is a valid mbid or not
    fun isValidMbidFormat(mbid: String): Boolean{
        //Must be 36 characters long (32 characters + 4 hyphens)
        if(mbid.length != 36) return false

        //Check if the 9th, 14th, 19th and 24th characters are hyphens
        if(mbid[8] != '-' || mbid[13] != '-' || mbid[18] != '-' || mbid[23] != '-') return false

        //Check if the rest of the characters are hexadecimal
        for(i in 0 until 36){
            if(i == 8 || i == 13 || i == 18 || i == 23) continue
            if(mbid[i] !in '0'..'9' && mbid[i] !in 'a'..'f' && mbid[i] !in 'A'..'F') return false
        }
        return true
    }

    fun ComponentActivity.askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }

    /**
     * Compares the current app version with the latest version from GitHub.
     * @param currentVersion The current app version (e.g., "2.8.3")
     * @param latestVersion The latest version from GitHub (e.g., "2.8.4-beta")
     * @return true if the latest version is newer than the current version
     */
    fun isNewerVersion(currentVersion: String, latestVersion: String?): Boolean {
        if (latestVersion.isNullOrBlank()) return false

        // Clean version strings (remove 'v' prefix if present)
        val cleanCurrentVersion = currentVersion.removePrefix("v")
        val cleanLatestVersion = latestVersion.removePrefix("v")

        try {
            // Split versions by dots to get major, minor, patch
            val currentParts = cleanCurrentVersion.split("-")[0].split(".")
            val latestParts = cleanLatestVersion.split("-")[0].split(".")

            // Compare major version
            val currentMajor = currentParts.getOrNull(0)?.toIntOrNull() ?: 0
            val latestMajor = latestParts.getOrNull(0)?.toIntOrNull() ?: 0
            if (latestMajor > currentMajor) return true
            if (latestMajor < currentMajor) return false

            // Compare minor version
            val currentMinor = currentParts.getOrNull(1)?.toIntOrNull() ?: 0
            val latestMinor = latestParts.getOrNull(1)?.toIntOrNull() ?: 0
            if (latestMinor > currentMinor) return true
            if (latestMinor < currentMinor) return false

            // Compare patch version
            val currentPatch = currentParts.getOrNull(2)?.toIntOrNull() ?: 0
            val latestPatch = latestParts.getOrNull(2)?.toIntOrNull() ?: 0
            return latestPatch > currentPatch

            // Versions are the same up to the patch level
        } catch (e: Exception) {
            android.util.Log.e("AppUpdatesViewModel", "Error comparing versions", e)
            return false
        }
    }

    fun submitLogs(context: Context) {
        Logger.apply {
            compressLogsInZipFile { zipFile ->
                zipFile?.let {
                    FileIntent
                        .fromFile(
                            context,
                            it,
                            BuildConfig.APPLICATION_ID
                        )
                        ?.let { intent ->
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Log Files")
                            intent.putExtra(
                                Intent.EXTRA_EMAIL,
                                arrayOf("mobile@metabrainz.org")
                            )
                            intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "Please find the attached log files."
                            )
                            intent.putExtra(
                                Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(
                                    context,
                                    "${BuildConfig.APPLICATION_ID}.provider",
                                    zipFile
                                )
                            )
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            try {
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Email logs..."
                                    )
                                )
                            } catch (e: java.lang.Exception) {
                                e(throwable = e)
                            }
                        }
                }
            }
        }
    }

    val Context.canShowNotifications get() =
        ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}