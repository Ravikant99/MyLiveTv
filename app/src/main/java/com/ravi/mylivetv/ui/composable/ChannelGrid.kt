package com.ravi.mylivetv.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ravi.mylivetv.domain.model.Channel

@Composable
fun ChannelGrid(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    columns: Int = 2,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState = rememberLazyGridState()
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCardItem(
                channel = channel,
                onClick = onChannelClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChannelGrid() {
    val sampleChannels = listOf(
        Channel("ESPN", "", "http://example.com/stream1", "Sports"),
        Channel("CNN", "", "http://example.com/stream2", "News"),
        Channel("HBO", "", "http://example.com/stream3", "Entertainment"),
        Channel("Disney", "", "http://example.com/stream4", "Kids")
    )
    ChannelGrid(channels = sampleChannels, onChannelClick = {})
}