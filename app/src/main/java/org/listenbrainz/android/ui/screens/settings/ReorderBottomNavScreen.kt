package org.listenbrainz.android.ui.screens.settings

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.navigation.BottomNavItem
import org.listenbrainz.android.ui.navigation.NavigationContent
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderBottomNavScreen(
    appPreferences: AppPreferences,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val storedItems by appPreferences.navBarOrder
        .getFlow()
        .collectAsState(
            initial = BottomNavItem.entries.toList()
        )
    var items by remember {
        mutableStateOf(storedItems)
    }
    val listState = rememberLazyListState()

    LaunchedEffect(storedItems) {
        items = storedItems
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Reorder Navigation") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavPreview(items = items.map { it.appNav })
        }
    ) { padding ->
        ReorderList(
            modifier = Modifier.padding(padding),
            items = items,
            listState = listState,
            onMove = { from, to ->
                items = items.toMutableList().apply {
                    add(to, removeAt(from))
                }
            },
            onDragEnd = {
                scope.launch {
                    appPreferences.navBarOrder.set(items)
                }
            }
        )
    }
}

@Composable
private fun BottomNavPreview(items: List<AppNavigationItem>) {
    BottomNavigation(
        backgroundColor = ListenBrainzTheme.colorScheme.nav,
        elevation = 0.dp
    ) {
        items.forEach {
            NavigationContent(
                item = it,
                selected = false,
                isLandscape = false,
                onItemClick = {},
                scope = this
            )
        }
    }
}

@Composable
private fun ReorderList(
    modifier: Modifier,
    listState: LazyListState,
    items: List<BottomNavItem>,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
) {
    var draggingItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    var targetIndex: Int by remember { mutableIntStateOf(0) }
    var delta: Float by remember {
        mutableFloatStateOf(0f)
    }
    var draggingItem: LazyListItemInfo? by remember {
        mutableStateOf(null)
    }
    val scrollChannel = Channel<Float>()

    LaunchedEffect(listState) {
        while (true) {
            val diff = scrollChannel.receive()
            listState.scrollBy(diff)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key1 = listState) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                            ?.also {
                                (it.contentType as? DraggableItem)?.let { draggableItem ->
                                    draggingItem = it
                                    draggingItemIndex = draggableItem.index
                                }
                            }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        delta += dragAmount.y

                        val currentDraggingItemIndex =
                            draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                        val currentDraggingItem =
                            draggingItem ?: return@detectDragGesturesAfterLongPress

                        val startOffset = currentDraggingItem.offset + delta
                        val endOffset =
                            currentDraggingItem.offset + currentDraggingItem.size + delta
                        val middleOffset = startOffset + (endOffset - startOffset) / 2

                        val targetItem =
                            listState.layoutInfo.visibleItemsInfo.find { item ->
                                middleOffset.toInt() in item.offset..item.offset + item.size &&
                                        currentDraggingItem.index != item.index &&
                                        item.contentType is DraggableItem
                            }

                        if (targetItem != null) {
                            targetIndex = (targetItem.contentType as DraggableItem).index
                            onMove(currentDraggingItemIndex, targetIndex)
                            draggingItemIndex = targetIndex
                            delta += currentDraggingItem.offset - targetItem.offset
                            draggingItem = targetItem
                        } else {
                            val startOffsetToTop =
                                startOffset - listState.layoutInfo.viewportStartOffset
                            val endOffsetToBottom =
                                endOffset - listState.layoutInfo.viewportEndOffset
                            val scroll =
                                when {
                                    startOffsetToTop < 0 -> startOffsetToTop.coerceAtMost(0f)
                                    endOffsetToBottom > 0 -> endOffsetToBottom.coerceAtLeast(0f)
                                    else -> 0f
                                }
                            val canScrollDown =
                                currentDraggingItemIndex != items.size - 1 && endOffsetToBottom > 0
                            val canScrollUp =
                                currentDraggingItemIndex != 0 && startOffsetToTop < 0
                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                scrollChannel.trySend(scroll)
                            }
                        }
                    },
                    onDragEnd = {
                        onDragEnd()
                        draggingItem = null
                        draggingItemIndex = null
                        delta = 0f
                    },
                    onDragCancel = {
                        draggingItem = null
                        draggingItemIndex = null
                        delta = 0f
                    },
                )
            },
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        itemsIndexed(
            items = items,
            contentType = { index, _ -> DraggableItem(index = index) },
        ) { index, item ->
            val modifier = if (draggingItemIndex == index) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        translationY = delta
                    }
            } else {
                Modifier
            }
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = ListenBrainzTheme.colorScheme.background,
                    contentColor = ListenBrainzTheme.colorScheme.text
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(item.appNav.iconUnselected),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.appNav.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.DragHandle, contentDescription = "Drag handle")
                }
            }
        }
    }
}

data class DraggableItem(val index: Int)