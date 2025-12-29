package com.ravi.mylivetv.ui.channel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravi.mylivetv.data.local.entity.RecentlyWatchedEntity
import com.ravi.mylivetv.domain.model.Channel
import com.ravi.mylivetv.domain.repository.ChannelRepository
import com.ravi.mylivetv.domain.repository.RecentlyWatchedRepository
import com.ravi.mylivetv.utils.CategoryMapper
import com.ravi.mylivetv.utils.Constants
import com.ravi.mylivetv.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val recentlyWatchedRepository: RecentlyWatchedRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "ChannelViewModel"
    
    private val _uiState = MutableStateFlow<Resource<List<Channel>>>(Resource.Loading)
    val uiState = _uiState.asStateFlow()
    
    // Track the current URL to avoid duplicate loads
    private var currentUrl: String? = null
    private var isLoading = false
    private var currentCategory: String? = null

    fun loadChannels(url: String, forceRefresh: Boolean = false, category: String = "") {
        currentCategory = category
        
        // Check if this is a "Recently Watched" category
        if (category == "Recently Watched") {
            loadRecentlyWatchedChannels()
            return
        }
        
        // Prevent duplicate loads for the same URL
        if (!forceRefresh && currentUrl == url && !isLoading && _uiState.value is Resource.Success) {
            Log.d(TAG, "⏭️ Skipping load - already loaded: $url")
            return
        }
        
        if (isLoading) {
            Log.d(TAG, "⏳ Already loading, skipping: $url")
            return
        }
        
        Log.d(TAG, "📡 Loading channels for: $url (forceRefresh: $forceRefresh)")
        
        viewModelScope.launch {
            isLoading = true
            _uiState.value = Resource.Loading
            currentUrl = url
            
            // Repository handles caching
            val result = repository.getChannels(url, forceRefresh)
            _uiState.value = result
            
            isLoading = false
            
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Loaded ${result.data.size} channels")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Failed to load channels: ${result.message}")
                }
                is Resource.Loading -> {
                    // Should not happen
                }
            }
        }
    }
    
    private fun loadRecentlyWatchedChannels() {
        Log.d(TAG, "Loading recently watched channels")
        
        viewModelScope.launch {
            try {
                _uiState.value = Resource.Loading
                
                // Get all recently watched channels, sorted by time
                val recentlyWatched = recentlyWatchedRepository.getAllRecentlyWatched(limit = 100)
                
                // Convert RecentlyWatchedEntity to Channel
                val channels = recentlyWatched.map { entity ->
                    Channel(
                        name = entity.channelName,
                        logo = entity.channelLogo,
                        streamUrl = entity.streamUrl,
                        category = entity.category
                    )
                }
                
                _uiState.value = if (channels.isEmpty()) {
                    Resource.Error("No recently watched channels", null)
                } else {
                    Resource.Success(channels)
                }
                
                Log.d(TAG, "✅ Loaded ${channels.size} recently watched channels")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load recently watched: ${e.message}", e)
                _uiState.value = Resource.Error("Failed to load recently watched channels", e)
            }
        }
    }
    
    fun refreshChannels() {
        if (currentCategory == "Recently Watched") {
            loadRecentlyWatchedChannels()
        } else {
            currentUrl?.let { url ->
                Log.d(TAG, "🔄 Force refreshing channels for: $url")
                loadChannels(url, forceRefresh = true, category = currentCategory ?: "")
            }
        }
    }
    
    fun clearCache() {
        Log.d(TAG, "🗑️ Clearing ViewModel state")
        currentUrl = null
        currentCategory = null
        _uiState.value = Resource.Loading
    }
}

