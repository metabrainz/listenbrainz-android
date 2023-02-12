package org.listenbrainz.android.data.di

import android.graphics.Bitmap

data class Card(val heading: String, val content: String, val image: Int)

data class user_profile(val name: String, val time: Long, val image: Int?)