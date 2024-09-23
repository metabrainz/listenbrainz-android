package org.listenbrainz.android.ui.screens.newsbrainz

import android.os.Build
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import org.listenbrainz.android.model.BlogPost
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.NewsListViewModel
import org.listenbrainz.android.R

@Composable
fun NewsBrainzScreen(
    viewModel: NewsListViewModel,
    onItemClicked: (BlogPost) -> Unit,
    onItemLongClicked: (BlogPost) -> Unit,
    onBack: () -> Unit
) {
    val posts = viewModel.blogPostsFlow.collectAsState().value

    LaunchedEffect(Unit){
        viewModel.fetchBlogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "NewsBrainz", color = MaterialTheme.colorScheme.onSurface) },
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(posts) { post ->
                    BlogCard(
                        post = post,
                        onClick = { onItemClicked(post) },
                        onLongClick = { onItemLongClicked(post) }
                    )
                }
            }
        }
    )

    // Loading Animation
    AnimatedVisibility(
        visible = viewModel.isLoading,
        enter = fadeIn(initialAlpha = 0.4f),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingAnimation()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlogCard(
    post: BlogPost,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            val parsedTitle: String = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    Html.fromHtml(post.title, Html.FROM_HTML_MODE_COMPACT).toString()
                }
                else -> {
                    HtmlCompat.fromHtml(post.title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                }
            }
            Row (verticalAlignment = Alignment.CenterVertically , modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween){
                Text(
                    text = parsedTitle,
                    textAlign = TextAlign.Left,
                    color = ListenBrainzTheme.colorScheme.lbSignature,
                    modifier = Modifier.padding(end = 5.dp).fillMaxWidth(0.9F)
                )
                IconButton(onClick = { onLongClick() }) {
                   Icon(painter = painterResource(id = R.drawable.news_share), contentDescription = "Share News Button" , tint = ListenBrainzTheme.colorScheme.hint , modifier = Modifier.padding(start = 5.dp)
                   )

                }
            }

            Spacer(Modifier.height(16.dp))

            val parsedContent: String = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    Html.fromHtml(post.content, Html.FROM_HTML_MODE_COMPACT).toString()
                }
                else -> {
                    HtmlCompat.fromHtml(post.content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                }
            }
            Text(
                text = parsedContent,
                maxLines = 4,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis

            )
        }
    }
}
