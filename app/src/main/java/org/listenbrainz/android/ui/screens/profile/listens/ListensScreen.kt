package org.listenbrainz.android.ui.screens.profile.listens

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.model.user.Artist
import org.listenbrainz.android.ui.components.ButtonLB
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.MusicBrainzButton
import org.listenbrainz.android.ui.components.SimilarUserCard
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.PersonalRecommendationDialog
import org.listenbrainz.android.ui.components.dialogs.PinDialog
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_mid
import org.listenbrainz.android.ui.theme.compatibilityMeterColor
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils.Spacer
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.util.consumeHorizontalDrag
import org.listenbrainz.android.util.optionalSharedElement
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
    snackbarState: SnackbarHostState,
    username: String?,
    goToArtistPage: (String) -> Unit,
    goToUserProfile: (String) -> Unit
) {
    val uiState by userViewModel.uiState.collectAsState()
    val preferencesUiState by viewModel.preferencesUiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()

    ListensScreen(
        scrollRequestState = scrollRequestState,
        onScrollToTop = onScrollToTop,
        username = username,
        uiState = uiState,
        preferencesUiState = preferencesUiState,
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
        onFollowButtonClick = { it, status ->
            if (!username.isNullOrEmpty()) {
                if (!status) {
                    userViewModel.followUser(it)
                } else {
                    userViewModel.unfollowUser(it)
                }
            }
        },
        goToArtistPage = goToArtistPage,
        goToUserProfile = goToUserProfile
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


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListensScreen(
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    username: String?,
    uiState: ProfileUiState,
    preferencesUiState: PreferencesUiState,
    playListen: (TrackMetadata) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    onFollowButtonClick: (username: String?, status: Boolean) -> Unit,
    goToArtistPage: (String) -> Unit,
    goToUserProfile: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    var similarUsersCollapsibleState by rememberSaveable {
        mutableStateOf(true)
    }

    var followersMenuState by rememberSaveable {
        mutableStateOf(true)
    }

    var followersMenuCollapsibleState by rememberSaveable {
        mutableStateOf(true)
    }

    var showAllListens by rememberSaveable {
        mutableStateOf(false)
    }

    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            listState.scrollToItem(0)
        }
    }

    if (uiState.listensTabUiState.isLoading)
        return

    SharedTransitionScope { sharedTransitionModifier ->
        AnimatedContent(
            modifier = Modifier
                .zIndex(69f)
                .then(sharedTransitionModifier),
            targetState = showAllListens
        ) { allListensVisible ->

            @Composable
            fun AnimatedVisibilityScope.ListenCardItem(
                listen: Listen,
                modifier: Modifier = Modifier
            ) {
                val metadata = remember(listen) {
                    listen.toMetadata()
                }

                ListenCardSmallDefault(
                    modifier = modifier
                        .padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        )
                        .optionalSharedElement(
                            sharedTransitionScope = this@SharedTransitionScope,
                            animatedVisibilityScope = this,
                            key = listen.sharedTransitionId
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

            @Composable
            fun RecentListensText(modifier: Modifier = Modifier) {
                Text(
                    text = "Recent Listens",
                    fontSize = 22.sp,
                    modifier = modifier
                        .padding(start = ListenBrainzTheme.paddings.horizontal)
                        .sharedElement(
                            sharedContentState = this@SharedTransitionScope
                                .rememberSharedContentState("recent listens"),
                            animatedVisibilityScope = this,
                        )
                )
            }

            if (allListensVisible) {
                BackHandler {
                    showAllListens = false
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeHorizontalDrag()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 10.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecentListensText(modifier = Modifier.weight(1f))

                        ButtonLB(
                            modifier = Modifier
                                .padding(end = ListenBrainzTheme.paddings.horizontal)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState("listens back"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                ),
                            onClick = {
                                showAllListens = false
                            }
                        ) {
                            Text(
                                modifier = Modifier.zIndex(1f),
                                text = "Back",
                                fontSize = 14.sp,
                            )
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = uiState
                                .listensTabUiState
                                .recentListens
                                .orEmpty()
                        ) { listen ->
                            ListenCardItem(listen = listen)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .testTag("listensScreenScrollableContainer")
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .padding(horizontal = ListenBrainzTheme.paddings.horizontal),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            if (uiState.isSelf) {
                                AddListensButton(modifier = Modifier)
                            } else {
                                FollowButton(
                                    modifier = Modifier,
                                    isFollowedState = uiState.listensTabUiState.isFollowing,
                                    onClick = {
                                        if (!uiState.listensTabUiState.isFollowing) {
                                            onFollowButtonClick(username ?: "", false)
                                        } else {
                                            onFollowButtonClick(username ?: "", true)
                                        }
                                    }
                                )
                            }

                            Spacer(8.dp)

                            val uriHandler = LocalUriHandler.current
                            var mbOpeningErrorState by remember {
                                mutableStateOf<String?>(null)
                            }

                            LaunchedEffect(mbOpeningErrorState) {
                                if (mbOpeningErrorState != null) {
                                    snackbarState.showSnackbar(
                                        "Some Error Occurred",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            MusicBrainzButton {
                                try {
                                    uriHandler.openUri(Constants.MB_BASE_URL + "user/${username}")
                                } catch (e: RuntimeException) {
                                    mbOpeningErrorState = e.message
                                } catch (e: Exception) {
                                    mbOpeningErrorState = e.message
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .padding(
                                    vertical = ListenBrainzTheme.paddings.sectionSeparation,
                                    horizontal = ListenBrainzTheme.paddings.horizontal
                                )
                                .shadow(4.dp, shape = ListenBrainzTheme.shapes.listenCardSmall)
                                .background(
                                    color = ListenBrainzTheme.colorScheme.level1,
                                    shape = ListenBrainzTheme.shapes.listenCardSmall
                                )
                                .padding(
                                    horizontal = ListenBrainzTheme.paddings.insideCard,
                                    vertical = ListenBrainzTheme.paddings.insideCard * 2
                                )
                        ) {
                            SongsListened(
                                username = username,
                                listenCount = uiState.listensTabUiState.listenCount,
                                isSelf = uiState.isSelf
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = ListenBrainzTheme.paddings.sectionSeparation),
                                color = ListenBrainzTheme.colorScheme.hint.copy(0.4f)
                            )

                            FollowersInformation(
                                followersCount = uiState.listensTabUiState.followersCount,
                                followingCount = uiState.listensTabUiState.followingCount
                            )
                        }
                    }

                    item {
                        RecentListensText(
                            modifier = Modifier
                                .padding(top = 30.dp, bottom = 10.dp)
                                .fillParentMaxWidth()
                                .clickable(interactionSource = null, indication = null) {
                                    showAllListens = true
                                }
                        )
                    }

                    items(
                        items = uiState
                            .listensTabUiState
                            .recentListens
                            ?.take(5)
                            .orEmpty()
                    ) { listen ->
                        ListenCardItem(listen)
                    }

                    item {
                        LoadMoreButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize()
                                .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                                .padding(top = 16.dp)
                                .zIndex(1f)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState("listens back"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                ),
                            state = true,
                            onClick = {
                                showAllListens = true
                            }
                        )
                    }

                    if (!uiState.isSelf) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp)
                                    .background(ListenBrainzTheme.colorScheme.songsListenedToBG)
                            ) {
                                Spacer(30.dp)
                                Text(
                                    "Your Compatibility",
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(start = ListenBrainzTheme.paddings.horizontal)
                                )
                                Spacer(10.dp)
                                CompatibilityCard(
                                    compatibility = uiState.listensTabUiState.compatibility ?: 0f,
                                    similarArtists = uiState.listensTabUiState.similarArtists,
                                    goToArtistPage = goToArtistPage
                                )
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 30.dp)
                                .fillMaxWidth()
                                .background(ListenBrainzTheme.colorScheme.songsListenedToBG)
                        ) {
                            Column {
                                FollowersCard(
                                    parentUsername = preferencesUiState.username,
                                    followersCount = uiState.listensTabUiState.followersCount,
                                    followingCount = uiState.listensTabUiState.followingCount,
                                    followers = when (followersMenuCollapsibleState) {
                                        true -> uiState.listensTabUiState.followers?.take(5)
                                            ?: emptyList()

                                        false -> uiState.listensTabUiState.followers ?: emptyList()
                                    },
                                    following = when (followersMenuCollapsibleState) {
                                        true -> uiState.listensTabUiState.following?.take(5)
                                            ?: emptyList()

                                        false -> uiState.listensTabUiState.following ?: emptyList()
                                    },
                                    followersState = followersMenuState,
                                    onStateChange = { newMenuState ->
                                        followersMenuState = !newMenuState
                                    },
                                    onFollowButtonClick = onFollowButtonClick,
                                    goToUserPage = { name ->
                                        if (name != null) {
                                            goToUserProfile(name)
                                        }
                                    }
                                )
                                if ((uiState.listensTabUiState.followersCount
                                        ?: 0) > 5 || ((uiState.listensTabUiState.followingCount
                                        ?: 0) > 5)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        LoadMoreButton(
                                            modifier = Modifier
                                                .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                                                .padding(top = 16.dp),
                                            state = followersMenuCollapsibleState,
                                            onClick = {
                                                followersMenuCollapsibleState =
                                                    !followersMenuCollapsibleState
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        if (!uiState.listensTabUiState.similarUsers.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 30.dp)
                                    .clip(shape = RoundedCornerShape(20.dp))
                                    .fillMaxWidth()
                                    .background(
                                        ListenBrainzTheme.colorScheme.songsListenedToBG
                                    )
                            ) {
                                Column {
                                    Spacer(30.dp)
                                    Text(
                                        "Similar Users",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(start = ListenBrainzTheme.paddings.horizontal)
                                    )
                                    Spacer(10.dp)
                                    SimilarUsersCard(
                                        similarUsers = when (similarUsersCollapsibleState) {
                                            true -> uiState.listensTabUiState.similarUsers.take(5)
                                            false -> uiState.listensTabUiState.similarUsers
                                        }, goToUserPage = { username ->
                                            if (username != null) {
                                                goToUserProfile(username)
                                            }
                                        }
                                    )
                                    if ((uiState.listensTabUiState.similarUsers.size) > 5) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            LoadMoreButton(
                                                modifier = Modifier
                                                    .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                                                    .padding(top = 16.dp),
                                                state = similarUsersCollapsibleState,
                                                onClick = {
                                                    similarUsersCollapsibleState =
                                                        !similarUsersCollapsibleState
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
        }
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)

    SuccessBar(
        resId = socialUiState.successMsgId,
        onMessageShown = onMessageShown,
        snackbarState = snackbarState
    )
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
                    if (artist.artistMbid != null) {
                        pushStringAnnotation(tag = "ARTIST", annotation = artist.artistMbid)
                    }
                    withStyle(style = SpanStyle(color = lb_purple_night)) {
                        append(artist.artistName)
                    }
                    if (artist.artistMbid != null) {
                        pop()
                    }
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
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal),
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
                fontSize = 14.sp,
                modifier = Modifier.padding(start = ListenBrainzTheme.paddings.horizontal, bottom = 8.dp),
            )
        }

        else -> {
            val text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = white)) {
                    append("You both listen to ")
                }
                similarArtists.forEachIndexed { index, artist ->
                    if (artist.artistMbid != null) {
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
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal),
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
    currentIndex: Int?,
    listens: List<Listen>,
    onPin: (metadata: Metadata, blurbContent: String) -> Unit,
    searchUsers: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String, metadata: Metadata) -> Unit,
    onPersonallyRecommend: (metadata: Metadata, users: List<String>, blurbContent: String) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    reviewEntityType: ReviewEntityType = ReviewEntityType.RECORDING
) {
    val context = LocalContext.current
    when (currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            PinDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                artistName = listens[currentIndex].trackMetadata.artistName,
                onDismiss = deactivateDialog,
                onSubmit = { blurbContent ->
                    onPin(listens[currentIndex].toMetadata(), blurbContent)
                })
            LaunchedEffect(socialUiState.error) {
                if (socialUiState.error == null) {
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
                onSubmit = { users, blurbContent ->
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
                onSubmit = { type, blurbContent, rating, locale ->
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
    modifier: Modifier = Modifier,
    state: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick, modifier = modifier.border(
            border = BorderStroke(
                1.dp,
                app_bg_mid
            ), shape = RoundedCornerShape(7.dp)
        )
    ) {
        Text(
            when (state) {
                true -> stringResource(R.string.load_more)
                false -> stringResource(R.string.load_less)
            }, color = app_bg_mid, style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun SongsListened(
    username: String?,
    listenCount: Int?,
    isSelf: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            when (isSelf) {
                true -> "You have listened to"
                false -> "$username has listened to"
            },
            color = ListenBrainzTheme.colorScheme.text,
            fontSize = 18.sp
        )

        Spacer(2.dp)

        Text(
            listenCount.toString(),
            color = ListenBrainzTheme.colorScheme.text,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            "songs so far",
            color = ListenBrainzTheme.colorScheme.hint,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FollowersInformation(
    followersCount: Int?,
    followingCount: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                (followersCount ?: 0).toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = ListenBrainzTheme.colorScheme.text,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "Followers",
                style = MaterialTheme.typography.bodyLarge,
                color = ListenBrainzTheme.colorScheme.text,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                (followingCount ?: 0).toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = ListenBrainzTheme.colorScheme.text,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "Following",
                style = MaterialTheme.typography.bodyLarge,
                color = ListenBrainzTheme.colorScheme.text,
            )
        }
    }
}

@Composable
fun ColumnScope.CompatibilityCard(
    compatibility: Float,
    similarArtists: List<Artist>,
    goToArtistPage: (String) -> Unit
) {
    Row(modifier = Modifier.padding(start = ListenBrainzTheme.paddings.horizontal)) {
        LinearProgressIndicator(
            progress = {
                compatibility
            }, color = compatibilityMeterColor, modifier = Modifier
                .height(17.dp)
                .fillMaxWidth(0.7f), strokeCap = StrokeCap.Round, trackColor = Color(0xFF1C1C1C)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            "${(compatibility * 100).toInt()} %",
            color = app_bg_mid,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
        )
    }
    Spacer(10.dp)
    BuildSimilarArtists(similarArtists = similarArtists, onArtistClick = { mbid ->
        goToArtistPage(mbid)
    })
}

@Composable
private fun FollowersCard(
    parentUsername: String,
    followersCount: Int?,
    followingCount: Int?,
    followers: List<Pair<String, Boolean>>,
    following: List<Pair<String, Boolean>>,
    followersState: Boolean,
    onStateChange: (Boolean) -> Unit,
    onFollowButtonClick: (String?, Boolean) -> Unit,
    goToUserPage: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(start = ListenBrainzTheme.paddings.horizontal, top = 30.dp, end = ListenBrainzTheme.paddings.horizontal)) {
        Text(
            text = if (followersState) "Followers" else "Followings",
            color = ListenBrainzTheme.colorScheme.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
        )
        Spacer(10.dp)
        Row {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (followersState) {
                        true -> ListenBrainzTheme.colorScheme.followerChipSelected
                        false -> ListenBrainzTheme.colorScheme.followerChipUnselected
                    },
                ),
                border = when (followersState) {
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
                border = when (followersState) {
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
        Spacer(10.dp)
        when (followersState) {
            true -> followers.map { state ->
                FollowCard(
                    parentUsername = parentUsername,
                    username = state.first,
                    onFollowButtonClick = onFollowButtonClick,
                    followStatus = state.second,
                    goToUserPage = goToUserPage
                )
                Spacer(10.dp)
            }

            false -> following.map { state ->
                FollowCard(
                    parentUsername = parentUsername,
                    username = state.first,
                    onFollowButtonClick = onFollowButtonClick,
                    followStatus = state.second,
                    goToUserPage = goToUserPage
                )
                Spacer(10.dp)
            }
        }
    }
}

@Composable
private fun ColumnScope.SimilarUsersCard(similarUsers: List<SimilarUser>, goToUserPage: (String?) -> Unit) {
    Spacer(20.dp)
    Text(
        "Similar Users",
        color = ListenBrainzTheme.colorScheme.text,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
        modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal)
    )
    Spacer(20.dp)
    similarUsers.mapIndexed { index, item ->
        SimilarUserCard(
            index = index,
            userName = item.username,
            similarity = item.similarity.toFloat(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            goToUserPage = goToUserPage
        )
    }
}

@Composable
private fun FollowCard(
    parentUsername: String,
    username: String?,
    onFollowButtonClick: (String?, Boolean) -> Unit,
    followStatus: Boolean,
    goToUserPage: (String?) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = ListenBrainzTheme.colorScheme.followerCardColor)) {
        Row(
            modifier = Modifier
                .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                .fillMaxWidth()
                .heightIn(min = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        goToUserPage(username)
                    },
                text = username ?: "",
                color = ListenBrainzTheme.colorScheme.followerCardTextColor,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
            if (parentUsername != username) {
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
}

@Composable
private fun AddListensButton(modifier: Modifier = Modifier) {
    ButtonLB(
        modifier = modifier,
        onClick = { /*TODO*/ }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(5.dp)
        Text(text = "Add Listens")
    }
}


@PreviewLightDark
@Composable
fun ListensScreenPreview() {
    PreviewSurface {
        ListensScreen(
            onScrollToTop = {},
            scrollRequestState = false,
            uiState = ListensScreenMockData.mockProfileUiStateOther,
            preferencesUiState = ListensScreenMockData.mockPreferencesUiState,
            playListen = {},
            socialUiState = ListensScreenMockData.mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            snackbarState = remember { SnackbarHostState() },
            username = "musiclover123",
            onFollowButtonClick = { _, _ -> },
            goToArtistPage = {},
            goToUserProfile = {}
        )
    }
}

@Preview(name = "Self Profile")
@Composable
fun ListensScreenSelfPreview() {
    PreviewSurface {
        ListensScreen(
            onScrollToTop = {},
            scrollRequestState = false,
            uiState = ListensScreenMockData.mockProfileUiStateSelf,
            preferencesUiState = ListensScreenMockData.mockPreferencesUiState,
            playListen = {},
            socialUiState = ListensScreenMockData.mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            snackbarState = remember { SnackbarHostState() },
            username = "pranavkonidena",
            onFollowButtonClick = { _, _ -> },
            goToArtistPage = {},
            goToUserProfile = {}
        )
    }
}

@Preview(name = "Following User")
@Composable
fun ListensScreenFollowingPreview() {
     PreviewSurface {
        ListensScreen(
            onScrollToTop = {},
            scrollRequestState = false,
            uiState = ListensScreenMockData.mockProfileUiStateFollowing,
            preferencesUiState = ListensScreenMockData.mockPreferencesUiState,
            playListen = {},
            socialUiState = ListensScreenMockData.mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            snackbarState = remember { SnackbarHostState() },
            username = "rockenthusiast",
            onFollowButtonClick = { _, _ -> },
            goToArtistPage = {},
            goToUserProfile = {}
        )
    }
}

@Preview(name = "Minimal Data")
@Composable
fun ListensScreenMinimalPreview() {
    PreviewSurface {
        ListensScreen(
            onScrollToTop = {},
            scrollRequestState = false,
            uiState = ListensScreenMockData.mockProfileUiStateMinimal,
            preferencesUiState = ListensScreenMockData.mockPreferencesUiState,
            playListen = {},
            socialUiState = ListensScreenMockData.mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            snackbarState = remember { SnackbarHostState() },
            username = "newuser",
            onFollowButtonClick = { _, _ -> },
            goToArtistPage = {},
            goToUserProfile = {}
        )
    }
}

@Preview(name = "No Data")
@Composable
fun ListensScreenNoDataPreview() {
    PreviewSurface {
        ListensScreen(
            onScrollToTop = {},
            scrollRequestState = false,
            uiState = ListensScreenMockData.mockProfileUiStateNoData,
            preferencesUiState = ListensScreenMockData.mockPreferencesUiState,
            playListen = {},
            socialUiState = ListensScreenMockData.mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            snackbarState = remember { SnackbarHostState() },
            username = "emptyuser",
            onFollowButtonClick = { _, _ -> },
            goToArtistPage = {},
            goToUserProfile = {}
        )
    }
}