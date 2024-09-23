package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> DialogSearchField(
    value: String,
    onValueChanged: (String) -> Unit,
    search: (String) -> Unit,
    searchResult: List<T>,
    placeholder: String = "Search",
    onItemClick: (T) -> Unit,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    rowItem: @Composable RowScope.(T) -> Unit
) {
    var isActive by remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        expanded = isActive,
        onExpandedChange = {
            //isActive = !isActive
        }
    ) {
        
        DialogTextField(
            value = value,
            onValueChange = {
                search(it)
                isActive = true
                onValueChanged(it)
            },
            singleLine = true,
            placeholder = placeholder,
            keyboardActions = keyboardActions
        )
        
        ExposedDropdownMenu(
            modifier = Modifier.background(ListenBrainzTheme.colorScheme.background),
            expanded = isActive && searchResult.isNotEmpty(),
            onDismissRequest = { isActive = false }
        ) {
            searchResult.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                        rowItem(this, item)
                    }
                }
            }
        }
        
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DialogSearchFieldPreview(){
    ListenBrainzTheme {
        DialogSearchField(
            value = "",
            onValueChanged = {
            
            },
            search = {},
            searchResult = listOf<String>()
            , 
            onItemClick = {
                
            }
        ) {
            Text(text = it)
        }
    }
}