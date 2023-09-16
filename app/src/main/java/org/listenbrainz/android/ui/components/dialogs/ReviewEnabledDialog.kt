package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewEnabledDialog(
    trackName: String? = null,
    artistName: String? = null,
    releaseName: String? = null,
    onDismiss: () -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSubmit: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) -> Unit
){
    var blurbContent by rememberSaveable {
        mutableStateOf("")
    }
    var selectedEntity by rememberSaveable {
        mutableStateOf(ReviewEntityType.RECORDING)
    }
    var rating by rememberSaveable {
        mutableStateOf(0f)
    }
    var selectedLocale by rememberSaveable {
        // TODO: Save previously used locale
        mutableStateOf(Locale.getDefault().language)
    }
    var isCheckBoxTicked by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    BaseDialog(
        onDismiss = onDismiss,
        
        title = {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_critiquebrainz_logo),
                contentDescription = "CritiqueBrainz Logo"
            )
        },
        
        content = {
            var expanded by remember { mutableStateOf(false) }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                
                DialogText("You are reviewing")
                
                Box {
                    Row(
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DialogText(
                            text = when (selectedEntity){
                                ReviewEntityType.RECORDING -> " $trackName (recording)"
                                ReviewEntityType.ARTIST -> " $artistName (artist)"
                                ReviewEntityType.RELEASE_GROUP -> " $releaseName (release group)"
                            },
                            bold = true
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Close dropdown" else "Show dropdown",
                            tint = ListenBrainzTheme.colorScheme.text
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Column {
                            arrayOf(trackName, artistName, releaseName).forEachIndexed { index, entityName ->
                                entityName?.let {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                when (index) {
                                                    0 -> selectedEntity = ReviewEntityType.RECORDING
                                                    1 -> selectedEntity = ReviewEntityType.ARTIST
                                                    2 -> selectedEntity =
                                                        ReviewEntityType.RELEASE_GROUP
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                        ) {
                                            DialogText(
                                                text = when (index){
                                                    0 -> "$entityName (recording)"
                                                    1 -> "$entityName (artist)"
                                                    2 -> "$entityName (release group)"
                                                    else -> ""
                                                },
                                                bold = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            val annotatedString = buildAnnotatedString {
                withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle().copy(color = ListenBrainzTheme.colorScheme.text)) {
                    append("for ")
                }
                pushStringAnnotation("redirect", "https://critiquebrainz.org/")
                withStyle(
                    ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()
                        .copy(color = ListenBrainzTheme.colorScheme.lbSignature)
                ) {
                    append("CritiqueBrainz")
                }
                pop()
                withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle()) {
                    append(".")
                }
            }
            
            ClickableText(text = annotatedString){ offset ->
                annotatedString.getStringAnnotations(tag = "redirect", start = offset, end = offset).firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            }
            
            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideDialog))
            
            DialogTextField(
                modifier = Modifier.height(160.dp),
                value = blurbContent,
                onValueChange = {
                     blurbContent = it
                },
                placeholder = "Review length must be at least 25 characters."
            )
    
            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideDialog))
            
            Row {
                DialogText(text = "Rating (Optional): ", bold = true)
                
                RatingBar(
                    modifier = Modifier.padding(start = 2.dp),
                    value = rating,
                    size = 19.dp,
                    spaceBetween = 2.dp,
                    style = RatingBarStyle.Default,
                    onValueChange = {
                        rating = it
                    },
                    onRatingChanged = {}
                )
            }
    
            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideDialog))
            
            var localeSearchText by remember {
                mutableStateOf("")
            }
            var localeSearchResult by remember {
                mutableStateOf(mapOf<String,String>())
            }
            val availableLocales = remember {
                Locale.getAvailableLocales()
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                DialogText(text = "Language: ", bold = true)
                
                DialogSearchField(
                    value = localeSearchText,
                    onValueChanged = {
                        localeSearchText = it
                    },
                    search = { text ->
                        localeSearchText = text
                        if (text.isEmpty()) {
                            localeSearchResult = emptyMap()
                            return@DialogSearchField
                        }
                        
                        // Searching for
                        scope.launch(Dispatchers.Default) {
                            localeSearchResult = availableLocales.associate {
                                Pair(it.displayLanguage, it.language)
                            }.filter { entry ->
                                entry.key.startsWith(text, ignoreCase = true)
                            }
                        }
                    },
                    placeholder = "Default: ${Locale.getDefault().displayLanguage}",
                    searchResult = localeSearchResult.entries.toList(),
                    onItemClick = { locale ->
                        keyboardController?.hide()
                        localeSearchText = locale.key
                        selectedLocale = locale.value
                        localeSearchResult = emptyMap()
                    },
                ) { locale ->
                    DialogText(text = locale.key)
                }
            }
    
            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideDialog))
            
            Row {
                Checkbox(
                    checked = isCheckBoxTicked,
                    onCheckedChange = {
                        isCheckBoxTicked = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ListenBrainzTheme.colorScheme.lbSignature,
                        checkmarkColor = ListenBrainzTheme.colorScheme.background
                    )
                )
                
                Text(
                    text = stringResource(R.string.cb_review_tc),
                    color = ListenBrainzTheme.colorScheme.text,
                    style = ListenBrainzTheme.textStyles.dialogText,
                    fontSize = 13.sp
                )
            }
            
            
        },
        
        footer = {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                DialogNegativeButton {
                    onDismiss()
                }
                
                Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.adjacentDialogButtons))
                
                DialogPositiveButton(
                    text = "Submit Review to CritiqueBrainz",
                    enabled = blurbContent.length >= 25 && isCheckBoxTicked
                ) {
                    onSubmit(selectedEntity, blurbContent, if (rating.toInt() == 0) null else rating.toInt(), selectedLocale)
                    onDismiss()
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReviewEnabledDialogPreview(){
    ListenBrainzTheme {
        ReviewEnabledDialog(
            trackName = "Gucci Chick",
            onDismiss = {},
            onSubmit = { _,_,_,_ ->
            
            }
        )
    }
}