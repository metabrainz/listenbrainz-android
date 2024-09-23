package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.StringSnapshotStateListSaver

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PersonalRecommendationDialog(
    trackName: String,
    onDismiss: () -> Unit,
    searchResult: List<String>,
    searchUsers: (String) -> Unit,
    onSubmit: (users: List<String>, blurbContent: String) -> Unit
) {
    val users = rememberSaveable(saver = StringSnapshotStateListSaver()) {
        mutableStateListOf()
    }
    var blurbContent by rememberSaveable {
        mutableStateOf("")
    }
    
    BaseDialog(
        onDismiss = onDismiss,
        title = {
            Text(
                text = buildAnnotatedString {
                    withStyle(ListenBrainzTheme.textStyles.dialogTitle.toSpanStyle()){
                        append("Recommend ")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogTitleBold.toSpanStyle()){
                        append(trackName)
                    }
                },
                color = ListenBrainzTheme.colorScheme.text
            )
        },
        
        content = {
            
            AnimatedContent(
                modifier = Modifier.height(46.dp),
                targetState = users.isNotEmpty(),
                label = ""
            ) {
                if(it){
                    LazyRow(Modifier.fillMaxWidth()) {
                        items(users){ user ->
                            UserTag(
                                user = user,
                                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, end = 6.dp),
                                showCrossButton = true,
                                onCrossButtonClick = {
                                    users.remove(user)
                                }
                            )
                        }
                    }
                } else {
                    DialogText(
                        // This custom padding is applied to accommodate the user tags.
                        modifier = Modifier.padding(
                            top = ListenBrainzTheme.paddings.insideDialog - ListenBrainzTheme.paddings.dialogContent/2,
                            bottom = ListenBrainzTheme.paddings.insideDialog + ListenBrainzTheme.paddings.dialogContent/2
                        ),
                        text = "Select followers to recommend this track to"
                    )
                }
            }
            
            
            var searchText by remember {
                mutableStateOf("")
            }
            
            DialogSearchField(
                value = searchText,
                onValueChanged = {
                    searchText = it
                },
                search = searchUsers,
                searchResult = searchResult,
                placeholder = "Add followers",
                onItemClick = { username ->
                    users.add(username)
                    searchText = ""
                    searchUsers("")
                }
            ) { username ->
                DialogText(username)
            }
            
            DialogText(
                modifier = Modifier.padding(vertical = ListenBrainzTheme.paddings.insideDialog),
                text = "Leave a message (optional)"
            )
            
            DialogTextField(
                modifier = Modifier.height(160.dp),
                value = blurbContent,
                onValueChange = {
                    if (it.length <= 280){
                        blurbContent = it
                    } },
                placeholder = "You will love this song because..."
            )
               
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ListenBrainzTheme.paddings.insideDialog),
                horizontalAlignment = Alignment.End
            ) {
                DialogText(text = "${blurbContent.length} / 280")
                Text(
                    text = "Can’t find a user? Make sure they are following you, and then try again.",
                    color = ListenBrainzTheme.colorScheme.text,
                    style = ListenBrainzTheme.textStyles.dialogText,
                    textAlign = TextAlign.End
                )
            }
            
        },
        
        footer = {
            Row(modifier = Modifier.align(CenterEnd)) {
                
                DialogNegativeButton(onDismiss = onDismiss)
                
                Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.adjacentDialogButtons))
        
                DialogPositiveButton(
                    text = "Send Recommendation",
                    enabled = users.isNotEmpty()
                ) {
                    onSubmit(users, blurbContent)
                    onDismiss()
                }
            }
        }
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PersonalRecommendationDialogPreview() {
    ListenBrainzTheme {
        var result = remember {
            mutableStateListOf<String>()
        }
        PersonalRecommendationDialog(
            trackName = "Gucci Chick",
            onDismiss = { /*TODO*/ },
            searchUsers = {
                if (result.lastIndex != -1){
                    result.removeRange(0, result.lastIndex)
                }
                result.addAll(listOf(it,it,it))
            },
            searchResult = result,
            onSubmit = {_,_ -> }
        )
    }
}