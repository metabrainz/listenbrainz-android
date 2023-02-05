package org.listenbrainz.android.presentation.features.brainzplayer.ui


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.listenbrainz.android.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Artist
import org.listenbrainz.android.data.sources.brainzplayer.Playlist
import org.listenbrainz.android.data.sources.brainzplayer.Playlist.Companion.recentlyPlayed
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.presentation.features.components.BrainzPlayerBottomBar
import org.listenbrainz.android.presentation.features.components.TopAppBar
import org.listenbrainz.android.presentation.features.brainzplayer.ui.album.AlbumViewModel
import org.listenbrainz.android.presentation.features.brainzplayer.ui.artist.ArtistViewModel
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.Navigation
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.forwardingPainter
import org.listenbrainz.android.presentation.features.brainzplayer.ui.playlist.PlaylistViewModel
import org.listenbrainz.android.presentation.features.listens.ListensActivity
import org.listenbrainz.android.presentation.theme.ListenBrainzTheme
import androidx.compose.material3.MaterialTheme
import org.listenbrainz.android.App
import org.listenbrainz.android.data.sources.brainzplayer.PlayableType

@ExperimentalPagerApi
@AndroidEntryPoint
class BrainzPlayerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListenBrainzTheme {
                val navController = rememberNavController()
                val albumViewModel = hiltViewModel<AlbumViewModel>()
                val artistViewModel = hiltViewModel<ArtistViewModel>()
                val playlistViewModel = hiltViewModel<PlaylistViewModel>()
                val artists = artistViewModel.artists.collectAsState(initial = listOf()).value
                val recentlyPlayed = Playlist.recentlyPlayed
                val albums = albumViewModel.albums.collectAsState(initial = listOf()).value
                val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
                val backdropScaffoldState =
                    rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            activity = this,
                            title = "BrainzPlayer"
                        )
                    },
                    bottomBar = { BrainzPlayerBottomBar(navController) },
                    backgroundColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    BrainzPlayerBackDropScreen(
                        backdropScaffoldState = backdropScaffoldState,
                        paddingValues = paddingValues,
                        backLayerContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Navigation(navController, albums, artists, playlists, recentlyPlayed, this@BrainzPlayerActivity)
                            }
                        })
                }
            }

        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: Playlist,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    navHostController: NavHostController,
    activity: BrainzPlayerActivity
) {
    val searchTextState = remember {
        mutableStateOf(TextFieldValue(""))
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchView(state = searchTextState, brainzPlayerViewModel)
            }
        }
        item {
            ListenBrainzHistoryCard(activity)
        }
        item {
            Column {
                Text(
                    text = "Recently Played",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow {
                    items(items = recentlyPlayedSongs.items) {
                        BrainzPlayerActivityCards(icon = it.albumArt,
                            errorIcon = R.drawable.ic_artist,
                            title = it.title,
                            modifier = Modifier
                                .clickable {
                                    brainzPlayerViewModel.changePlayable(recentlyPlayedSongs.items, PlayableType.ALL_SONGS, it.mediaID,recentlyPlayedSongs.items.sortedBy { it.discNumber }.indexOf(it))
                                    brainzPlayerViewModel.playOrToggleSong(it, true)
                                }
                        )
                    }
                }
            }
        }
        item {
            Column {
                Text(
                    text = "Artists",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow {
                    items(items = artists) {
                        BrainzPlayerActivityCards(icon = "",
                            errorIcon = R.drawable.ic_artist,
                            title = it.name,
                            modifier = Modifier
                                .clickable {
                                    navHostController.navigate("onArtistClick/${it.id}")
                                }
                        )
                    }
                }
            }
        }
        item {
            Column {
                Text(
                    text = "Albums",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow {
                    items(albums) {
                        BrainzPlayerActivityCards(it.albumArt,
                            R.drawable.ic_album,
                            title = it.title,
                            modifier = Modifier
                                .clickable {
                                    navHostController.navigate("onAlbumClick/${it.albumId}")
                                }
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Playlists",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow {
                    items(playlists.filter {
                        it.id != (-1).toLong()
                    }) {
                        BrainzPlayerActivityCards(
                            icon = "",
                            errorIcon = it.art,
                            title = it.title,
                            modifier = Modifier.clickable { navHostController.navigate("onPlaylistClick/${it.id}") })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchView(state: MutableState<TextFieldValue>, brainzPlayerViewModel: BrainzPlayerViewModel) {
    var searchStarted by remember {
        mutableStateOf(false)
    }
    var searchItems by remember {
        mutableStateOf(mutableListOf<Song>())
    }
    val itemHeights = remember { mutableStateMapOf<Int, Int>() }
    val baseHeight = 330.dp
    val density = LocalDensity.current
    val maxHeight = remember(itemHeights.toMap()) {
        if (itemHeights.keys.toSet() != searchItems.indices.toSet ()) {
            // if we don't have all heights calculated yet, return default value
            return@remember baseHeight
        }
        val baseHeightInt = with(density) { baseHeight.toPx().toInt() }
        var sum = with(density) { 8.dp.toPx().toInt() } * 2
        for ((_, itemSize) in itemHeights.toSortedMap()) {
            sum += itemSize
            if (sum >= baseHeightInt) {
                return@remember with(density) { (sum - itemSize / 2).toDp() }
            }
        }
        // all items fit into base height
        baseHeight
    }

    Box {
        TextField(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(2.dp),
            value = state.value,
            onValueChange = { value ->
                state.value = value
                searchItems = brainzPlayerViewModel.searchSongs(value.text)!!.toMutableList()
                searchStarted = true

            },
            textStyle = TextStyle(MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(15.dp)
                        .size(24.dp)

                )
            },
            trailingIcon = {
                if (state.value != TextFieldValue("")) {
                    IconButton(onClick = {
                        state.value = TextFieldValue("")
                    }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(15.dp)
                                .size(24.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.textFieldColors(

                textColor = Color.Black,
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
    DropdownMenu(
        modifier = Modifier.requiredSizeIn(maxHeight = maxHeight),
        properties = PopupProperties(focusable = false ),
        expanded = searchStarted,
        onDismissRequest = { searchStarted = false }) {
        searchItems.forEachIndexed { index, song ->

            DropdownMenuItem(
                modifier = Modifier.onSizeChanged {
                        itemHeights[index] = it.height

                },
                onClick = {
                    brainzPlayerViewModel.changePlayable(listOf(song), PlayableType.SONG, song.mediaID,0)
                    brainzPlayerViewModel.playOrToggleSong(song, true)
                searchStarted = false
                state.value.text.removeRange(0, state.value.text.length-1.coerceAtLeast(0))}) {
                androidx.compose.material3.Text(text = song.title )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListenBrainzHistoryCard(activity: BrainzPlayerActivity) {
    val gradientColors =
        Brush.horizontalGradient(0f to Color(0xff353070), 1000f to Color(0xffFFA500))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(gradientColors)
            .height(120.dp)
            .clickable {
                activity.startActivity(Intent(activity as Activity, ListensActivity::class.java))
            },
    ) {
        Column {
            Icon(
                imageVector = Icons.Rounded.PlayArrow, contentDescription = "",
                Modifier
                    .size(30.dp)
                    .padding(start = 3.dp, top = 3.dp), tint = Color.White
            )
            Text(
                text = "Listen to \nplayback history",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecentlyPlayedCard() {
    val gradientColors = Brush.verticalGradient(0f to Color(0xff263238), 100f to Color(0xff324147))
    Box(
        modifier = Modifier
            .height(175.dp)
            .width(180.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(gradientColors)
            .border(color = Color(0xff324147), width = 1.dp, shape = RoundedCornerShape(8.dp))
    )
}

@Composable
fun BrainzPlayerActivityCards(icon: String, errorIcon : Int, title: String, modifier : Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(200.dp)
            .width(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = modifier
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                    .size(150.dp)
            ) {
                AsyncImage(
                    modifier = modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .clip(CircleShape),
                    model = icon,
                    contentDescription = "",
                    error = forwardingPainter(
                        painter = painterResource(id = errorIcon)
                    ) { info ->
                        inset(25f, 25f) {
                            with(info.painter) {
                                draw(size, info.alpha, info.colorFilter)
                            }
                        }
                    },
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}