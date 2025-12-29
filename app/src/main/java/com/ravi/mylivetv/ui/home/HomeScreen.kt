package com.ravi.mylivetv.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ravi.mylivetv.navigation.ScreenRoutes
import com.ravi.mylivetv.ui.composable.CategoryItem
import com.ravi.mylivetv.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab,
        pageCount = { Constants.TABS.size }
    )
    val scope = rememberCoroutineScope()
    
    // Flag to prevent circular updates
    var isUserScrolling by remember { mutableStateOf(false) }

    // Sync pager state with view model (only when user swipes, not when clicking tabs)
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isUserScrolling = true
        }
        
        if (!pagerState.isScrollInProgress && isUserScrolling && pagerState.currentPage != uiState.selectedTab) {
            viewModel.selectTab(pagerState.currentPage)
            isUserScrolling = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB8E6B8)) // Light green background
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "HomeScreen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Constants.TABS.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                
                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) 
                        Color(0xFF2191E5) // Darker blue for selected
                    else 
                        Color(0xFFADD8E6), // Light blue
                    animationSpec = tween(300),
                    label = "tabBackgroundColor"
                )
                
                // Animate elevation
                val elevation by animateDpAsState(
                    targetValue = if (isSelected) 4.dp else 0.dp,
                    animationSpec = tween(300),
                    label = "tabElevation"
                )
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (index != pagerState.currentPage) {
                                scope.launch {
                                    viewModel.selectTab(index)
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                    color = backgroundColor,
                    shadowElevation = elevation
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) 
                            FontWeight.Bold 
                        else 
                            FontWeight.Medium,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        // Content with HorizontalPager for swipe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFADD8E6)) // Light blue background
                .padding(8.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val listState = rememberLazyListState()
                
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Category/Language/Country Items (including "Recently Watched" as first item)
                    val items = when (page) {
                        0 -> Constants.CATEGORIES
                        1 -> Constants.LANGUAGES
                        2 -> Constants.COUNTRIES
                        else -> Constants.CATEGORIES
                    }
                    itemsIndexed(items) { index, item ->
                        CategoryItem(
                            text = item,
                            isSelected = uiState.selectedItemIndex == index && page == uiState.selectedTab,
                            onClick = {
                                viewModel.selectItem(index)
                                // Navigate to channel screen with the category
                                navController.navigate(ScreenRoutes.ChannelScreen.createRoute(item))
                            }
                        )
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    val navController = rememberNavController()
    // Note: Preview won't work with HiltViewModel
}


