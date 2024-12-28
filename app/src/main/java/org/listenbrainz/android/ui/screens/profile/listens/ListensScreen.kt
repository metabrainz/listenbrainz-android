package org.listenbrainz.android.ui.screens.profile.listens

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.model.user.Artist
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.SimilarUserCard
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.PersonalRecommendationDialog
import org.listenbrainz.android.ui.components.dialogs.PinDialog
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_mid
import org.listenbrainz.android.ui.theme.compatibilityMeterColor
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun ListensScreen(
    viewModel: ListensViewModel,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    snackbarState : SnackbarHostState,
    username: String?,
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String?) -> Unit
) {
    
    val uiState by userViewModel.uiState.collectAsState()
    val preferencesUiState by viewModel.preferencesUiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()

    ListensScreen(
        scrollRequestState = scrollRequestState,
        onScrollToTop = onScrollToTop,
        username= username,
        uiState = uiState,
        preferencesUiState = preferencesUiState,
        updateNotificationServicePermissionStatus = {
            viewModel.updateNotificationServicePermissionStatus()
        },
        validateUserToken = { token ->
            viewModel.validateUserToken(token)
        },
        setToken = {
            viewModel.setAccessToken(it)
        },
        playListen = {
            socialViewModel.playListen(it)
        },
        snackbarState = snackbarState,
        socialUiState = socialUiState,
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        onFollowButtonClick = {
            it, status ->
            if(!username.isNullOrEmpty()) {
                if(!status){
                    userViewModel.followUser(it)
                }
                else{
                    userViewModel.unfollowUser(it)
                }
            }
        },
        goToArtistPage = goToArtistPage,
        goToUserPage = goToUserPage
    )
}

enum class ListenDialogBundleKeys {
    PAGE,
    EVENT_INDEX;
    companion object {
        fun listenDialogBundle(page: Int, eventIndex: Int): Bundle {
            return Bundle().apply {
                putInt(PAGE.name, page)
                putInt(EVENT_INDEX.name, eventIndex)
            }
        }
    }
}



@Composable
fun ListensScreen(
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    username: String?,
    uiState: ProfileUiState,
    preferencesUiState: PreferencesUiState,
    updateNotificationServicePermissionStatus: () -> Unit,
    validateUserToken: suspend (String) -> Boolean,
    setToken: (String) -> Unit,
    playListen: (TrackMetadata) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onErrorShown : () -> Unit,
    onMessageShown : () -> Unit,
    onFollowButtonClick: (String?, Boolean) -> Unit,
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String?) -> Unit,
) {
    val listState = rememberLazyListState()

    var recentListensCollapsibleState by remember {
        mutableStateOf(true)
    }

    val similarUsersCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val followersMenuState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val followersMenuCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    
    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            listState.scrollToItem(0)
        }
    }

        AnimatedVisibility(visible = !uiState.listensTabUiState.isLoading) {
            LazyColumn(state = listState, modifier = Modifier.testTag("listensScreenScrollableContainer")) {
                item {
                    SongsListened(username = username, listenCount = uiState.listensTabUiState.listenCount, isSelf = uiState.isSelf)
                }
                item{
                    FollowersInformation(followersCount = uiState.listensTabUiState.followersCount, followingCount = uiState.listensTabUiState.followingCount)
                }
                item{
                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Recent Listens",
                        color = ListenBrainzTheme.colorScheme.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(
                    items = if(recentListensCollapsibleState) {
                        uiState.listensTabUiState.recentListens?.take(5)
                    } else {
                        uiState.listensTabUiState.recentListens?.take(10)
                    } ?: listOf()
                ) { listen  ->
                    val metadata = listen.toMetadata()

                    ListenCardSmallDefault(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                            ),
                        metadata = metadata,
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = listen.trackMetadata.mbidMapping?.caaReleaseMbid,
                            caaId = listen.trackMetadata.mbidMapping?.caaId
                        ),
                        onDropdownError = { error ->
                            snackbarState.showSnackbar(error.toast)
                        },
                        onDropdownSuccess = { message ->
                            snackbarState.showSnackbar(message)
                        },
                        goToArtistPage = goToArtistPage,
                    ) {
                        playListen(listen.trackMetadata)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadMoreButton(
                            state = recentListensCollapsibleState,
                            onClick = {
                                recentListensCollapsibleState = !recentListensCollapsibleState
                            }
                        )
                    }
                }

                if (!uiState.isSelf) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp)
                                .background(ListenBrainzTheme.colorScheme.songsListenedToBG)
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(30.dp))
                                Text("Your Compatibility", color = ListenBrainzTheme.colorScheme.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp), modifier = Modifier.padding(start = 16.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                CompatibilityCard(compatibility = uiState.listensTabUiState.compatibility ?: 0f, uiState.listensTabUiState.similarArtists, goToArtistPage = goToArtistPage)
                            }
                        }
                    }   
                }

                item {
                    Box(modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .background(ListenBrainzTheme.colorScheme.songsListenedToBG)
                    ) {
                        Column {
                            FollowersCard(
                                followersCount = uiState.listensTabUiState.followersCount,
                                followingCount = uiState.listensTabUiState.followingCount,
                                followers = when(followersMenuCollapsibleState.value){
                                    true -> uiState.listensTabUiState.followers?.take(5) ?: emptyList()
                                    false -> uiState.listensTabUiState.followers ?: emptyList()
                                },
                                following = when(followersMenuCollapsibleState.value){
                                    true -> uiState.listensTabUiState.following?.take(5) ?: emptyList()
                                    false -> uiState.listensTabUiState.following ?: emptyList()
                                },
                                followersState = followersMenuState.value,
                                onStateChange = {
                                        newMenuState->
                                    followersMenuState.value = !newMenuState
                                },
                                onFollowButtonClick = onFollowButtonClick,
                                goToUserPage = goToUserPage
                            )
                            if((uiState.listensTabUiState.followersCount ?: 0) > 5 || ((uiState.listensTabUiState.followingCount ?: 0) > 5)){
                                Spacer(modifier = Modifier.height(20.dp))
                                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    LoadMoreButton(
                                        state = followersMenuCollapsibleState.value,
                                        onClick = {
                                            followersMenuCollapsibleState.value = !followersMenuCollapsibleState.value
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    if(!uiState.listensTabUiState.similarUsers.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 30.dp)
                                .clip(shape = RoundedCornerShape(20.dp))
                                .fillMaxWidth()
                                .background(
                                    ListenBrainzTheme.colorScheme.songsListenedToBG
                                )
                        ) {
                            Column {
                                SimilarUsersCard(
                                    similarUsers = when (similarUsersCollapsibleState.value) {
                                        true -> uiState.listensTabUiState.similarUsers.take(5)

                                        false -> uiState.listensTabUiState.similarUsers
                                    }, goToUserPage = goToUserPage
                                )

                                if ((uiState.listensTabUiState.similarUsers.size) > 5) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        LoadMoreButton(
                                            state = similarUsersCollapsibleState.value,
                                            onClick = {
                                                similarUsersCollapsibleState.value =
                                                    !similarUsersCollapsibleState.value
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)

        SuccessBar(resId = socialUiState.successMsgId, onMessageShown = onMessageShown, snackbarState = snackbarState)
}

@Composable
private fun BuildSimilarArtists(similarArtists: List<Artist>, onArtistClick: (String) -> Unit) {
    val white = Color.White

    when {
        similarArtists.size > 5 -> {
            val topSimilarArtists = similarArtists.take(5)
            val text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = lb_purple_night)) {
                    append("You both listen to ")
                }
                topSimilarArtists.forEachIndexed { index, artist ->
                    if(artist.artistMbid != null) {
                        pushStringAnnotation(tag = "ARTIST", annotation = artist.artistMbid)
                    }
                    withStyle(style = SpanStyle(color = lb_purple_night)) {
                        append(artist.artistName)
                    }
                    pop()
                    if (index < topSimilarArtists.size - 1) {
                        withStyle(style = SpanStyle(color = lb_purple_night)) {
                            append(", ")
                        }

                    }
                }
                withStyle(style = SpanStyle(color = lb_purple_night)) {
                    append(" and more.")
                }
            }
            ClickableText(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { offset ->
                    text.getStringAnnotations(tag = "ARTIST", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            onArtistClick(annotation.item)
                        }
                }
            )
        }
        similarArtists.isEmpty() -> {
            Text(
                "You have no common artists",
                color = ListenBrainzTheme.colorScheme.text,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        else -> {
            val text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = white)) {
                    append("You both listen to ")
                }
                similarArtists.forEachIndexed { index, artist ->
                    if(artist.artistMbid != null) {
                        pushStringAnnotation(tag = "ARTIST", annotation = artist.artistMbid)
                    }
                    withStyle(style = SpanStyle(color = lb_purple_night)) {
                        append(artist.artistName)
                    }
                    if (artist.artistMbid != null) {
                        pop()
                    }
                    if (index < similarArtists.size - 1) {
                        append(", ")
                    }
                }
            }
            ClickableText(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    text.getStringAnnotations(tag = "ARTIST", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            onArtistClick(annotation.item)
                        }
                }
            )
        }
    }
}


@Composable
fun Dialogs(
    deactivateDialog: () -> Unit,
    currentDialog: Dialog,
    feedUiState: FeedUiState,
    currentIndex : Int?,
    listens : List<Listen>,
    onPin : (metadata: Metadata , blurbContent : String) -> Unit,
    searchUsers : (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview : (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String , metadata : Metadata) -> Unit,
    onPersonallyRecommend : (metadata : Metadata , users : List<String> , blurbContent : String) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    reviewEntityType: ReviewEntityType = ReviewEntityType.RECORDING
) {
    val context = LocalContext.current
    when (currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            PinDialog(trackName = listens[currentIndex!!].trackMetadata.trackName, artistName = listens[currentIndex].trackMetadata.artistName, onDismiss = deactivateDialog, onSubmit = {
                blurbContent ->
                onPin(listens[currentIndex].toMetadata(), blurbContent)
            })
            LaunchedEffect(socialUiState.error){
                if(socialUiState.error == null){
                    snackbarState.showSnackbar(context.getString(R.string.pin_greeting))
                }
            }
        }
        Dialog.PERSONAL_RECOMMENDATION -> {
            PersonalRecommendationDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                onDismiss = deactivateDialog,
                searchResult = feedUiState.searchResult,
                searchUsers = searchUsers,
                onSubmit = {
                    users, blurbContent ->
                    onPersonallyRecommend(
                        listens[currentIndex].toMetadata(),
                        users,
                        blurbContent
                    )
                }
            )
        }
        Dialog.REVIEW -> {
            ReviewDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                artistName = listens[currentIndex].trackMetadata.artistName,
                releaseName = listens[currentIndex].trackMetadata.releaseName,
                onDismiss = deactivateDialog,
                isCritiqueBrainzLinked = isCritiqueBrainzLinked,
                onSubmit  = { type, blurbContent, rating, locale ->
                    onReview(
                        type,
                        blurbContent,
                        rating,
                        locale,
                        listens[currentIndex].toMetadata()
                    )
                },
                reviewEntityType = reviewEntityType
            )
        }
    }
}

@Composable
fun LoadMoreButton(
    state: Boolean,
    onClick : () -> Unit,
){
    TextButton(onClick, modifier = Modifier.border(border = BorderStroke(1.dp,
        app_bg_mid), shape = RoundedCornerShape(7.dp)
    )) {
        Text(when(state){
            true -> "Load More"
            false -> "Load Less"
        }, color = app_bg_mid, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
private fun SongsListened(username: String? , listenCount: Int?, isSelf: Boolean){
    Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .background(ListenBrainzTheme.colorScheme.songsListenedToBG)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            when(isSelf){
                true -> "You have listened to"
                false -> "$username has listened to"
            }
            , color = ListenBrainzTheme.colorScheme.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp))
        Spacer(modifier = Modifier.height(15.dp))
        HorizontalDivider(color = ListenBrainzTheme.colorScheme.dividerColor, modifier = Modifier.padding(start = 60.dp, end = 60.dp))
        Spacer(modifier = Modifier.height(15.dp))
        Text(listenCount.toString(), color = ListenBrainzTheme.colorScheme.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),  textAlign = TextAlign.Center)
        Text("songs so far", color = app_bg_mid, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(30.dp))
    }

}

@Composable
private fun FollowersInformation(followersCount: Int?, followingCount: Int?){
    Box(modifier = Modifier
        .background(
            ListenBrainzTheme.colorScheme.userPageGradient
        )
    ){
        Row (horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 30.dp)) {
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Text((followersCount ?:0).toString(), style = MaterialTheme.typography.bodyLarge, color = ListenBrainzTheme.colorScheme.text)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Followers", style = MaterialTheme.typography.bodyLarge, color = ListenBrainzTheme.colorScheme.text)
            }
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Text((followingCount ?: 0).toString(), style = MaterialTheme.typography.bodyLarge, color = ListenBrainzTheme.colorScheme.text)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Following", style = MaterialTheme.typography.bodyLarge, color = ListenBrainzTheme.colorScheme.text)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }


}

@Composable
fun CompatibilityCard(compatibility: Float, similarArtists: List<Artist>, goToArtistPage: (String) -> Unit){
    Row (modifier = Modifier.padding(start = 16.dp)) {
        LinearProgressIndicator(progress = {
            compatibility
        }, color = compatibilityMeterColor, modifier = Modifier
            .height(17.dp)
            .fillMaxWidth(0.7f), strokeCap = StrokeCap.Round, trackColor = Color(0xFF1C1C1C))
        Spacer(modifier = Modifier.width(9.dp))
        Text("${(compatibility*100).toInt()} %", color = app_bg_mid, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp))
    }
    Spacer(modifier = Modifier.height(10.dp))
    BuildSimilarArtists(similarArtists = similarArtists, onArtistClick = {
        mbid ->
        goToArtistPage(mbid)
    })
}

@Composable
private fun FollowersCard(followersCount: Int?, followingCount: Int?, followers: List<Pair<String,Boolean>>,
                          following: List<Pair<String,Boolean>>, followersState: Boolean, onStateChange: (Boolean) -> Unit,
                          onFollowButtonClick: (String?, Boolean) -> Unit, goToUserPage: (String?) -> Unit) {
    Column(modifier = Modifier.padding(start = 16.dp , top = 30.dp, end = 16.dp)) {
        Text(
            "Followers",
            color = ListenBrainzTheme.colorScheme.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (followersState) {
                        true -> ListenBrainzTheme.colorScheme.followerChipSelected
                        false -> ListenBrainzTheme.colorScheme.followerChipUnselected
                    },
                ),
                border = when(followersState){
                    true -> null
                    false -> BorderStroke(width = 1.dp, color = lb_purple_night)
                },
                modifier = Modifier
                    .width(120.dp)
                    .height(30.dp)
                    .clickable {
                        onStateChange(followersState)
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Followers (${followersCount})",
                        color = when (followersState) {
                            true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                            false -> ListenBrainzTheme.colorScheme.followerChipSelected
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (followersState) {
                        true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                        false -> ListenBrainzTheme.colorScheme.followerChipSelected
                    },
                ),
                border = when(followersState){
                    true -> BorderStroke(width = 1.dp, color = lb_purple_night)
                        false -> null
                },
                modifier = Modifier
                    .width(120.dp)
                    .height(30.dp)
                    .clickable {
                        onStateChange(followersState)
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Following (${followingCount})",
                        color = when (followersState) {
                            true -> ListenBrainzTheme.colorScheme.followerChipSelected
                            false -> ListenBrainzTheme.colorScheme.followerChipUnselected
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        when(followersState){
            true -> followers.map {
                state ->
                FollowCard(username = state.first, onFollowButtonClick = onFollowButtonClick, followStatus = state.second, goToUserPage = goToUserPage)
                Spacer(modifier = Modifier.height(10.dp))
            }
            false -> following.map {
                state ->
                FollowCard(username = state.first, onFollowButtonClick = onFollowButtonClick, followStatus = state.second, goToUserPage = goToUserPage)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SimilarUsersCard(similarUsers: List<SimilarUser>, goToUserPage: (String?) -> Unit){
    Spacer(modifier = Modifier.height(20.dp))
    Text("Similar Users", color = ListenBrainzTheme.colorScheme.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp), modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(modifier = Modifier.height(20.dp))
    similarUsers.mapIndexed{
        index , item ->
        SimilarUserCard(index = index, userName = item.username, similarity = item.similarity.toFloat(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), goToUserPage = goToUserPage)
    }
}

@Composable
private fun FollowCard(username: String?, onFollowButtonClick: (String?, Boolean) -> Unit, followStatus: Boolean, goToUserPage: (String?) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = ListenBrainzTheme.colorScheme.followerCardColor)) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                username ?: "",
                color = ListenBrainzTheme.colorScheme.followerCardTextColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable {
                    goToUserPage(username)
                }
            )

            FollowButton(
                modifier = Modifier,
                isFollowedState = followStatus,
                onClick = {
                    onFollowButtonClick(username, followStatus)
                }
            )
        }
    }
}


@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(
        onScrollToTop = {},
        scrollRequestState = false,
        updateNotificationServicePermissionStatus = {},
        uiState = ProfileUiState(),
        preferencesUiState = PreferencesUiState(),
        validateUserToken = { true },
        setToken = {},
        playListen = {},
        socialUiState = SocialUiState(),
        onErrorShown = {},
        onMessageShown = {},
        snackbarState = remember { SnackbarHostState() },
        username = "pranavkonidena",
        onFollowButtonClick = {_,_ -> },
        goToArtistPage = {},
        goToUserPage = {}
    )
}
