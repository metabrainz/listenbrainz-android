package org.listenbrainz.shared.util

import android.content.pm.PackageManager
import org.listenbrainz.shared.repository.PlatformContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

actual object PlatformUtils {
    actual fun getSHA1(context: PlatformContext, packageName: String): String? {
        try {
            val signatures = context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures ?: emptyArray()

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
}