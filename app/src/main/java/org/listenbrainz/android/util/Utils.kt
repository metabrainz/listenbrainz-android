package org.listenbrainz.android.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
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

    fun urlIntent(url: String?): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun sendToPicard(context: Context, releaseMBID: String){
        val ipAddress = UserPreferences.preferenceIpAddress
        if(ipAddress==null){
            Toast.makeText(context,"Add your IP Address in the settings, matched according to your Picard network", Toast.LENGTH_LONG).show()
            return
        }
        //To allow http requests specially
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val url = String.format(
            App.PICARD_OPENALBUM_URL, ipAddress,
            UserPreferences.preferencePicardPort, uriEncode(releaseMBID)
        )

        CoroutineScope(context = Dispatchers.Default).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Do you have your Picard running?", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if(response.code==200){
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Release sent to your Picard!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun uriEncode(releaseId: String): String {
        return try {
            URLEncoder.encode(releaseId, "UTF-8")
        }
        catch (e: UnsupportedEncodingException) {
            Log.e(this.javaClass.name, e.message, e)
            URLEncoder.encode(releaseId)
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
}