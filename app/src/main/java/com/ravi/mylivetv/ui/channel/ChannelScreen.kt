package com.ravi.mylivetv.ui.channel

import android.content.res.Configuration
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ravi.mylivetv.domain.model.Channel
import com.ravi.mylivetv.ui.composable.ChannelCardItem
import com.ravi.mylivetv.ui.composable.ChannelGrid
import com.ravi.mylivetv.utils.CategoryMapper
import com.ravi.mylivetv.utils.Resource

@Composable
fun ChannelScreen(
    navController: NavController,
    category: String,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Get configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    
    // Keyboard controller and focus manager for Android TV
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }
    
    // Calculate grid columns based on screen size and orientation
    val gridColumns = when {
        screenWidthDp >= 1200 -> 6  // Extra large screens (TV/Desktop)
        screenWidthDp >= 840 -> 5   // Large tablets landscape
        screenWidthDp >= 600 && isLandscape -> 4  // Tablets landscape
        screenWidthDp >= 600 -> 3   // Tablets portrait
        isLandscape -> 3            // Phone landscape
        else -> 2                   // Phone portrait
    }
    
    // Retain scroll position
    val gridState = rememberLazyGridState()
    
    // Load channels only once when screen is first composed or category changes
    LaunchedEffect(category) {
        if (category == "Recently Watched") {
            // For Recently Watched, we don't need a URL
            viewModel.loadChannels("", forceRefresh = false, category = category)
        } else {
            val url = when {
                CategoryMapper.isCategory(category) -> CategoryMapper.getCategoryUrl(category)
                CategoryMapper.isLanguage(category) -> CategoryMapper.getLanguageUrl(category)
                CategoryMapper.isCountry(category) -> CategoryMapper.getCountryUrl(category)
                else -> CategoryMapper.getCategoryUrl(category)
            }
            // Will use cached data if available, won't make API call
            viewModel.loadChannels(url, forceRefresh = false, category = category)
        }
    }

    // Determine category URL for recently watched (skip if it's "Recently Watched" itself)
    val categoryUrl = remember(category) {
        if (category == "Recently Watched") {
            ""
        } else {
            when {
                CategoryMapper.isCategory(category) -> CategoryMapper.getCategoryUrl(category)
                CategoryMapper.isLanguage(category) -> CategoryMapper.getLanguageUrl(category)
                CategoryMapper.isCountry(category) -> CategoryMapper.getCountryUrl(category)
                else -> CategoryMapper.getCategoryUrl(category)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB8E6B8)) // Light green background
            .padding(20.dp)
    ) {
        // Title with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Text(
                text = category,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        // Search bar with icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF8DC))
                    .padding(10.dp)
                    .focusRequester(searchFocusRequester)
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                        // Don't show keyboard automatically on focus (for Android TV)
                        if (!focusState.isFocused) {
                            keyboardController?.hide()
                        }
                    },
                placeholder = { Text("Search your channel") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFF8DC),
                    unfocusedContainerColor = Color(0xFFFFF8DC),
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                // Disable auto-show keyboard on Android TV
                readOnly = false
            )
        }
        
        // Channels Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFADD8E6)) // Light blue background
                .padding(8.dp)
        ) {
            when (uiState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val channels = (uiState as Resource.Success<List<Channel>>).data
                    val filteredChannels = channels.filter { 
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                    
                    if (filteredChannels.isEmpty()) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No channels available" else "No channels found for \"$searchQuery\"",
                            modifier = Modifier.align(Alignment.Center)
                                .padding(bottom = 100.dp),
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Store channel list for navigation
                        LaunchedEffect(filteredChannels) {
                            com.ravi.mylivetv.utils.ChannelListHolder.setChannels(filteredChannels, category)
                        }
                        
                        ChannelGrid(
                            channels = filteredChannels,
                            gridState = gridState,
                            columns = gridColumns,
                            onChannelClick = { channel ->
                                // Find channel index in filtered list
                                val channelIndex = filteredChannels.indexOfFirst { it.streamUrl == channel.streamUrl }
                                
                                // Navigate to player screen with stream URL, channel name, logo, category, categoryUrl, and channel index
                                navController.navigate(
                                    com.ravi.mylivetv.navigation.ScreenRoutes.PlayerScreen.createRoute(
                                        streamUrl = channel.streamUrl,
                                        channelName = channel.name,
                                        logoUrl = channel.logo,
                                        category = category,
                                        categoryUrl = categoryUrl,
                                        channelIndex = channelIndex
                                    )
                                )
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load channels",
                            fontSize = 16.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as Resource.Error).message ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (category == "Recently Watched") {
                                viewModel.loadChannels("", forceRefresh = true, category = category)
                            } else {
                                val url = when {
                                    CategoryMapper.isCategory(category) -> CategoryMapper.getCategoryUrl(category)
                                    CategoryMapper.isLanguage(category) -> CategoryMapper.getLanguageUrl(category)
                                    CategoryMapper.isCountry(category) -> CategoryMapper.getCountryUrl(category)
                                    else -> CategoryMapper.getCategoryUrl(category)
                                }
                                // Force refresh on retry
                                viewModel.loadChannels(url, forceRefresh = true, category = category)
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChannelScreen() {
    val navController = rememberNavController()
    ChannelScreen(navController, category = "Animation")
}


