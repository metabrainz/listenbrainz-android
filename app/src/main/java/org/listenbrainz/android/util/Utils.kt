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