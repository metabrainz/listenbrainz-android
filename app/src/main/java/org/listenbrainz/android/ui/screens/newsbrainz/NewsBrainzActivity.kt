package org.listenbrainz.android.ui.screens.newsbrainz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.aemerse.share.SharableItem
import com.aemerse.share.Share
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.viewmodel.NewsListViewModel

class NewsBrainzActivity : ComponentActivity() {

    private val viewModel: NewsListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ListenBrainzTheme {
                NewsBrainzScreen(
                    modifier = Modifier.safeDrawingPadding(),
                    viewModel = viewModel,
                    onItemClicked = { post ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.URL))
                        startActivity(intent)
                    },
                    onItemLongClicked = { post ->
                        Share.with(context = this)
                            .item(SharableItem(
                                pictureUrl = null,
                                data = post.URL + "\n",
                                shareAppLink = true,
                                downloadOurAppMessage = "Download our app"
                            ),
                                onStart = { Log.d("onStart Sharing") },
                                onFinish = { isSuccessful: Boolean, errorMessage: String ->
                                    when {
                                        isSuccessful -> {
                                            Log.d("Successfully shared")
                                        }
                                        else -> {
                                            Log.e("error happened : $errorMessage")
                                        }
                                    }
                                }
                            )
                    }
                ) {
                    finish()
                }
            }
        }
    }
}