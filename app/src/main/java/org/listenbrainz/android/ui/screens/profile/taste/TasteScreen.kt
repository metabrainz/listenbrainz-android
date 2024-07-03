package org.listenbrainz.android.ui.screens.profile.taste

import LovedHated
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.ProfileViewModel

@Composable
fun TasteScreen(
    viewModel: ProfileViewModel,
    uiState: ProfileUiState,
) {
    val lovedHatedState: MutableState<LovedHated> = remember { mutableStateOf(LovedHated.loved) }
    val lovedHatedCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }

    LazyColumn {
        item {
            Row (modifier = Modifier.padding(start = 16.dp)) {
                ElevatedSuggestionChip(
                    onClick = { lovedHatedState.value = LovedHated.loved },
                    label = {
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Text("Loved", color = when(lovedHatedState.value == LovedHated.loved){
                                true -> Color.Black
                                false -> lb_purple_night
                            })
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.heart),
                                contentDescription = "",
                                modifier = Modifier.height(15.dp),
                                tint = when(lovedHatedState.value == LovedHated.loved){
                                    true -> Color.Black
                                    false -> lb_purple_night
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = when(lovedHatedState.value == LovedHated.loved){
                        true -> null
                            false -> BorderStroke(1.dp, lb_purple_night)
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (lovedHatedState.value == LovedHated.loved) {
                            ListenBrainzTheme.colorScheme.followerChipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.followerChipUnselected
                        }
                    ),
                    )
                Spacer(modifier = Modifier.width(10.dp))
                ElevatedSuggestionChip(
                    onClick = { lovedHatedState.value = LovedHated.hated },
                    label = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hated", color = when(lovedHatedState.value == LovedHated.hated){
                            true -> Color.Black
                            false -> lb_purple_night
                        })
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(Icons.Default.HeartBroken, contentDescription = "", modifier = Modifier.height(15.dp), tint = when(lovedHatedState.value == LovedHated.hated){
                            true -> Color.Black
                            false -> lb_purple_night
                        })
                    } },
                    shape = RoundedCornerShape(10.dp),
                    border = when(lovedHatedState.value == LovedHated.hated){
                        true -> null
                        false -> BorderStroke(1.dp, lb_purple_night)
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (lovedHatedState.value == LovedHated.hated) {
                            ListenBrainzTheme.colorScheme.followerChipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.followerChipUnselected
                        }
                    ),
                    )
            }
        }
        itemsIndexed(items = when(lovedHatedState.value){
            LovedHated.loved -> when(lovedHatedCollapsibleState.value){
                true -> uiState.tasteTabUIState.lovedSongs?.feedback?.take(5) ?: listOf()
                false -> uiState.tasteTabUIState.lovedSongs?.feedback ?: listOf()
            }
            LovedHated.hated -> when(lovedHatedCollapsibleState.value){
                true -> uiState.tasteTabUIState.hatedSongs?.feedback?.take(5) ?: listOf()
                false -> uiState.tasteTabUIState.hatedSongs?.feedback ?: listOf()
            }
        }){
            index, feedback ->
            ListenCardSmall(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                trackName = feedback.trackMetadata?.trackName ?: "", artistName = feedback.trackMetadata
                ?.artistName ?: "", coverArtUrl = getCoverArtUrl(
                caaReleaseMbid = feedback.trackMetadata?.mbidMapping?.caaReleaseMbid,
                caaId = feedback.trackMetadata?.mbidMapping?.caaId
            ),
                enableDropdownIcon = true,
                onDropdownIconClick = {
                    dropdownItemIndex.value = index
                },
                ) {
            }
        }
        item{
            if((uiState.tasteTabUIState.lovedSongs?.count
                    ?: 0) > 5 || (uiState.tasteTabUIState.hatedSongs?.count ?: 0) > 5
            ){
                Spacer(modifier = Modifier.height(20.dp))
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LoadMoreButton(state = lovedHatedCollapsibleState.value) {
                        lovedHatedCollapsibleState.value = !lovedHatedCollapsibleState.value
                    }
                }

            }
        }
    }
}