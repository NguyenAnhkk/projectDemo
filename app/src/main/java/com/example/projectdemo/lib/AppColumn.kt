package com.example.projectdemo.lib

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp

@Composable
inline fun AppColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier,
        verticalArrangement,
        horizontalAlignment,
        content
    )
}


@Composable
fun <T> AppColumnScroll(
    listItem: List<T>?,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    isLoadMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    itemLoadMore: @Composable LazyItemScope.() -> Unit = {},
    itemContent: @Composable LazyItemScope.(index: Int, model: T?) -> Unit
) {
    //todo scroll hide keyboard
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
        }
    }

    LazyColumn(

        modifier.nestedScroll(nestedScrollConnection), state, contentPadding, reverseLayout, verticalArrangement, horizontalAlignment,
        flingBehavior, userScrollEnabled,
    ) {
        items(listItem?.size ?: 0) { idx ->
            itemContent(idx, listItem?.getOrNull(idx))
        }

        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isLoadMore,
                enter = slideInVertically(
                    initialOffsetY = {
                        -it
                    },
                ),
                exit = slideOutVertically(
                    targetOffsetY = {
                        0
                    },
                ),
                modifier = modifier,
            ){
                itemLoadMore()
            }
        }

    }
    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.collect { lastVisibleItemIndex ->
            if (lastVisibleItemIndex == (listItem?.size ?: 0) - 1 && !isLoadMore) {
                onLoadMore()
            }
        }
    }

}
