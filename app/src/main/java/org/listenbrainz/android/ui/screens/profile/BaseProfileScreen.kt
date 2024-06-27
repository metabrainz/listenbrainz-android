package org.listenbrainz.android.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.listens.ListensScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_light
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Constants

@Composable
fun BaseProfileScreen(
    username: String?,
    snackbarState: SnackbarHostState,
    uiState: ProfileUiState,
    onFollowClick: (String) -> Unit,
    onUnfollowClick: (String) -> Unit,
    goToUserProfile: () -> Unit,
){

    val currentTab : MutableState<ProfileScreenTab> = remember { mutableStateOf(ProfileScreenTab.LISTENS) }
    val isLoggedInUser = uiState.listensTabUiState.isSelf
    val uriHandler = LocalUriHandler.current
    val mbOpeningErrorState = remember {
        mutableStateOf<String?>(null)
    }
    Box(modifier = Modifier.fillMaxSize()){
        AnimatedVisibility(
            visible = uiState.listensTabUiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
        AnimatedVisibility(visible = !uiState.listensTabUiState.isLoading) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ListenBrainzTheme.colorScheme.background,
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal / 2))
                    repeat(5) { position ->
                        ElevatedSuggestionChip(
                            modifier = Modifier.padding(ListenBrainzTheme.paddings.chipsHorizontal),
                            colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                if (position == currentTab.value.index) {
                                    ListenBrainzTheme.colorScheme.chipSelected
                                } else {
                                    if(position == 0){
                                        if(uiState.listensTabUiState.isSelf){
                                            lb_purple
                                        }
                                        else{
                                            lb_orange
                                        }
                                    }
                                    else{
                                        ListenBrainzTheme.colorScheme.chipUnselected
                                    }

                                }
                            ),
                            shape = ListenBrainzTheme.shapes.chips,
                            elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(elevation = 4.dp),
                            icon = {
                                if(position == 0 && !uiState.listensTabUiState.isSelf){
                                    Box (modifier = Modifier
                                        .background(lb_purple)
                                        .padding(4.dp)) {
                                        Icon(Icons.Default.Home, contentDescription = "", tint = new_app_bg_light, modifier = Modifier.clickable {
                                            goToUserProfile()
                                        })
                                    }

                                }
                            },
                            label = {
                                Text(
                                    text = when (position) {
                                        0 -> username ?: ""
                                        1 -> ProfileScreenTab.LISTENS.value
                                        2 -> ProfileScreenTab.STATS.value
                                        3 -> ProfileScreenTab.PLAYLISTS.value
                                        4 -> ProfileScreenTab.TASTE.value
                                        5 -> ProfileScreenTab.CREATED_FOR_YOU.value
                                        else -> ""
                                    },
                                    style = ListenBrainzTheme.textStyles.chips,
                                    color = ListenBrainzTheme.colorScheme.text,
                                )
                            },
                            onClick = { currentTab.value = when (position) {
                                1 -> ProfileScreenTab.LISTENS
                                2 -> ProfileScreenTab.STATS
                                3 -> ProfileScreenTab.PLAYLISTS
                                4 -> ProfileScreenTab.TASTE
                                5 -> ProfileScreenTab.CREATED_FOR_YOU
                                else -> ProfileScreenTab.LISTENS
                            } }
                        )
                    }
                }


                Row (modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 20.dp)) {
                    when(isLoggedInUser) {
                        true -> AddListensButton()
                        false -> when(uiState.listensTabUiState.isFollowing){
                            true -> UnFollowButton(username = username, onUnFollowClick = {
                                onUnfollowClick(it)
                            })
                            false -> FollowButton(username = username, onFollowClick = {
                                onFollowClick(it)
                            })
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    MusicBrainzButton(){
                       try {
                           uriHandler.openUri(Constants.MB_BASE_URL + "user/${username}")
                       }
                       catch (e: RuntimeException) {
                           mbOpeningErrorState.value = e.message;
                       }
                       catch (e: Exception){
                           mbOpeningErrorState.value = e.message;
                       }
                    }
                }
                when(currentTab.value) {
                    ProfileScreenTab.LISTENS -> ListensScreen(
                        scrollRequestState = false,
                        onScrollToTop = {},
                        snackbarState = snackbarState,
                        username = username
                    )
                    else -> ListensScreen(
                        scrollRequestState = false,
                        onScrollToTop = {},
                        snackbarState = snackbarState,
                        username = username
                    )
                }

            }
        }
        if(mbOpeningErrorState.value != null){
            LaunchedEffect(mbOpeningErrorState.value) {
                snackbarState.showSnackbar("Some Error Occoued", duration = SnackbarDuration.Short)
            }
        }

    }

}

@Composable
private fun FollowButton(onFollowClick: (String) -> Unit, username: String?) {
    IconButton(onClick = {
        if(!username.isNullOrEmpty()){
            onFollowClick(username)
        }

    }, modifier = Modifier
        .background(lb_purple)
        .width(100.dp)
        .height(30.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(painter = painterResource(id = R.drawable.follow_icon), contentDescription = "", tint = app_bg_light, modifier = Modifier
                .width(20.dp)
                .height(20.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Follow", color = new_app_bg_light, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun UnFollowButton(onUnFollowClick: (String) -> Unit, username: String?) {
    IconButton(onClick = {
        if(!username.isNullOrEmpty()){
            onUnFollowClick(username)
        }

    }, modifier = Modifier
        .background(lb_purple)
        .width(100.dp)
        .height(30.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(painter = painterResource(id = R.drawable.follow_icon), contentDescription = "", tint = new_app_bg_light, modifier = Modifier
                .width(20.dp)
                .height(20.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Unfollow", color = new_app_bg_light, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddListensButton() {
    IconButton(onClick = { /*TODO*/ }, modifier = Modifier
        .background(Color(0xFF353070))
        .width(110.dp)
        .height(30.dp)) {
        Row(modifier = Modifier.padding(all = 4.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "", tint = new_app_bg_light, modifier = Modifier
                .width(10.dp)
                .height(20.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Add Listens", color = new_app_bg_light, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MusicBrainzButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier
        .background(Color(0xFF353070))
        .width(140.dp)
        .height(30.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Icon(painter = painterResource(id = R.drawable.musicbrainz_logo), contentDescription = "", modifier = Modifier
                .width(20.dp)
                .height(30.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(5.dp))
            Text("MusicBrainz", color = new_app_bg_light, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(5.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "", tint = new_app_bg_light, modifier = Modifier
                .width(30.dp)
                .height(20.dp))
        }
    }
}
