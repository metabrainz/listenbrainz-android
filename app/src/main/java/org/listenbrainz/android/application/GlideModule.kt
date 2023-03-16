package org.listenbrainz.android.application

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class GlideModule : AppGlideModule() {
    /* Stops unnecessary logs from glide about no content found.*/
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }
}