package org.listenbrainz.android.ui.screens.newsbrainz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aemerse.share.SharableItem
import com.aemerse.share.Share
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.e
import org.listenbrainz.android.viewmodel.NewsListViewModel

@AndroidEntryPoint
class NewsBrainzActivity : AppCompatActivity() {

    private lateinit var viewModel: NewsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[NewsListViewModel::class.java]

        setContent {
            ListenBrainzTheme {
                NewsBrainzScreen(
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
                                onStart = { d("onStart Sharing") },
                                onFinish = { isSuccessful: Boolean, errorMessage: String ->
                                    when {
                                        isSuccessful -> {
                                            d("Successfully shared")
                                        }
                                        else -> {
                                            e("error happened : $errorMessage")
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