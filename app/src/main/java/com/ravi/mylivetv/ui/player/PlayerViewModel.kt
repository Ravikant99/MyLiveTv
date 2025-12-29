package com.ravi.mylivetv.ui.player

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.ravi.mylivetv.domain.repository.RecentlyWatchedRepository
import com.ravi.mylivetv.utils.PlayerErrorHandler
import com.ravi.mylivetv.utils.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlayerViewModel"

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val recentlyWatchedRepository: RecentlyWatchedRepository
) : AndroidViewModel(application) {

    private var _player: ExoPlayer? = null
    val player: ExoPlayer?
        get() = _player

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var currentStreamUrl: String? = null
    private var savedPlaybackPosition: Long = 0L
    private var savedPlayWhenReady: Boolean = true
    private var isPlayerInitialized = false
    
    // Channel navigation state
    private var currentChannelIndex: Int = -1
    private var channelList: List<com.ravi.mylivetv.domain.model.Channel> = emptyList()

//    private val _isPlaying = MutableStateFlow(false)
//    val isPlaying = _isPlaying.asStateFlow()



    fun initializePlayer(
        streamUrl: String, 
        channelName: String = "", 
        logoUrl: String = "", 
        category: String = "", 
        categoryUrl: String = "",
        channelIndex: Int = -1
    ) {
        // Prevent multiple initializations
        if (isPlayerInitialized && currentStreamUrl == streamUrl) {
            Log.d(TAG, "Player already initialized for this stream, skipping")
            return
        }

        Log.d(TAG, "Initializing player for: $streamUrl")
        
        // Release existing player if switching streams
        if (currentStreamUrl != streamUrl && _player != null) {
            Log.d(TAG, "Releasing previous player for different stream")
            releasePlayer()
        }

        currentStreamUrl = streamUrl
        
        // Load channel list from holder if channelIndex is valid
        if (channelIndex >= 0) {
            channelList = com.ravi.mylivetv.utils.ChannelListHolder.getChannels()
            currentChannelIndex = channelIndex
            Log.d(TAG, "Channel list loaded: ${channelList.size} channels, current index: $currentChannelIndex")
        }
        
        // Update UI state with channel info
        _uiState.value = _uiState.value.copy(
            channelName = channelName,
            channelLogoUrl = logoUrl,
            isBuffering = true,
            hasStartedPlaying = false, // Reset for new stream
            category = category,
            categoryUrl = categoryUrl,
            hasNextChannel = hasNextChannel(),
            hasPreviousChannel = hasPreviousChannel()
        )

        if (_player == null) {
            Log.d(TAG, "Creating new ExoPlayer instance")
            val trackSelector = createTrackSelector(getApplication())
            val loadControl = createLoadControl()
            
            // Configure audio attributes for audio focus
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            _player = ExoPlayer.Builder(getApplication())
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true) // true = handle audio focus automatically
                .setHandleAudioBecomingNoisy(true) // Pause when headphones are disconnected
                .build().apply {

                    seekTo(savedPlaybackPosition)
                    playWhenReady = savedPlayWhenReady
                    
                    // Ensure volume is set properly
                    volume = 1.0f

                    addListener(playerListener)
                    addAnalyticsListener(analyticsListener)
                    PlayerErrorHandler.logStreamInfo(streamUrl, "LOADING")
                    
                    // Configure MediaItem for live streaming
                    val mediaItem = MediaItem.Builder()
                        .setUri(streamUrl)
                        .setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder()
                                .setMaxPlaybackSpeed(1.02f)
                                .setMinPlaybackSpeed(0.98f)
                                .setTargetOffsetMs(3000)         // Reduced from 5s to 3s - closer to live edge
                                .setMinOffsetMs(1000)            // Reduced from 2s to 1s
                                .setMaxOffsetMs(8000)            // Reduced from 10s to 8s
                                .build()
                        )
                        .build()
                    
                    setMediaItem(mediaItem)
                    prepare()
                }
            
            isPlayerInitialized = true
        }
    }

    fun releasePlayer() {
        Log.d(TAG, "Releasing player")
        _player?.let {
            savedPlaybackPosition = it.currentPosition
            savedPlayWhenReady = it.playWhenReady
            it.removeListener(playerListener)
            it.release()
        }
        _player = null
        isPlayerInitialized = false
        currentStreamUrl = null
        
        // Reset UI state
        _uiState.value = PlayerUiState()
    }


    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            viewModelScope.launch {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        PlayerErrorHandler.logPlaybackState("IDLE")
                        _uiState.value = _uiState.value.copy(isBuffering = false)
                    }
                    Player.STATE_BUFFERING -> {
                        PlayerErrorHandler.logPlaybackState("BUFFERING")
                        _uiState.value = _uiState.value.copy(
                            isBuffering = true,
                            errorMessage = null
                        )
                    }
                    Player.STATE_READY -> {
                        PlayerErrorHandler.logPlaybackState("READY")
                        _uiState.value = _uiState.value.copy(
                            isBuffering = false,
                            isPlaying = true,
                            errorMessage = null,
                            hasStartedPlaying = true // Mark that playback has started
                        )
                        
                        // Save to recently watched when playback starts successfully
                        saveToRecentlyWatched()
                    }
                    Player.STATE_ENDED -> {
                        PlayerErrorHandler.logPlaybackState("ENDED")
                        _uiState.value = _uiState.value.copy(
                            isBuffering = false,
                            isPlaying = false
                        )
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            viewModelScope.launch {
                // Handle "behind live window" error by seeking to live edge
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    Log.d(TAG, "Behind live window error - seeking to live edge and retrying")
                    _player?.let { player ->
                        player.seekToDefaultPosition()
                        player.prepare()
                        player.play()
                    }
                } else {
                    val errorMessage = PlayerErrorHandler.getErrorMessage(error)
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }
        }
        
        override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
            // Log available audio tracks for debugging
            val audioTracks = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
            Log.d(TAG, "Audio tracks changed. Found ${audioTracks.size} audio track groups")
            
            audioTracks.forEachIndexed { groupIndex, trackGroup ->
                Log.d(TAG, "Audio Group $groupIndex: ${trackGroup.length} tracks, selected=${trackGroup.isSelected}")
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getTrackFormat(i)
                    Log.d(TAG, "  Track $i: ${format.sampleMimeType}, channels=${format.channelCount}, " +
                            "sampleRate=${format.sampleRate}, bitrate=${format.bitrate}, " +
                            "language=${format.language}, selected=${trackGroup.isTrackSelected(i)}")
                }
            }
            
            // If no audio track is selected, log warning
            if (audioTracks.none { it.isSelected }) {
                Log.w(TAG, "⚠️ WARNING: No audio track is currently selected!")
            }
        }

    }

    private val analyticsListener = @UnstableApi
    object : androidx.media3.exoplayer.analytics.AnalyticsListener {
        override fun onLoadStarted(
            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
            loadEventInfo: androidx.media3.exoplayer.source.LoadEventInfo,
            mediaLoadData: androidx.media3.exoplayer.source.MediaLoadData
        ) {
            Log.d(TAG, "Load started: ${loadEventInfo.uri}")
        }

        @OptIn(UnstableApi::class)
        override fun onLoadCompleted(
            eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
            loadEventInfo: androidx.media3.exoplayer.source.LoadEventInfo,
            mediaLoadData: androidx.media3.exoplayer.source.MediaLoadData
        ) {
            PlayerErrorHandler.logChunkLoad(loadEventInfo.uri.toString(), loadEventInfo.bytesLoaded)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createTrackSelector(context: Context): DefaultTrackSelector {
        return DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    // Video settings
                    .setMaxVideoBitrate(3_500_000) // Increased from 2.5Mbps to 3.5Mbps for better quality
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setPreferredVideoMimeType(MimeTypes.VIDEO_H264)
                    // Audio settings - critical for audio playback
                    .setAllowAudioMixedMimeTypeAdaptiveness(true)
                    .setAllowAudioMixedChannelCountAdaptiveness(true) // Allow different channel counts
                    .setForceHighestSupportedBitrate(false) // Don't force highest bitrate
                    .setMaxAudioBitrate(320_000) // Allow up to 320kbps audio
                    .setMaxAudioChannelCount(8) // Support up to 7.1 surround sound
                    .setPreferredAudioLanguage("en") // Fallback language
                    .setSelectUndeterminedTextLanguage(true) // Select tracks with undetermined language
            )
        }
    }


    @OptIn(UnstableApi::class)
    private fun createLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                50_000,  // minBuffer → increased from 30s to 50s for more stability
                180_000, // maxBuffer → increased from 120s to 180s for better prefetch
                2_500,   // start playback → increased from 1.5s to 2.5s for smoother start
                5_000    // rebuffer threshold → increased from 3s to 5s to reduce rebuffering
            )
            .setPrioritizeTimeOverSizeThresholds(false) // Changed to false for better quality
            .build()
    }



    fun pausePlayer() {
        _player?.let {
            savedPlaybackPosition = it.currentPosition
            savedPlayWhenReady = it.playWhenReady
            it.pause()
            Log.d(TAG, "Player paused, position saved: $savedPlaybackPosition")
        }
    }

    fun resumePlayer() {
        _player?.let { player ->
            // For live streams, seek to live edge to avoid "behind live window" errors
            if (player.isCurrentMediaItemLive) {
                Log.d(TAG, "Resuming live stream - seeking to live edge")
                player.seekToDefaultPosition()
                player.playWhenReady = true
            } else {
                player.playWhenReady = savedPlayWhenReady
                Log.d(TAG, "Player resumed, restoring position: $savedPlaybackPosition")
            }
        }
    }

    fun retryPlayback() {
        currentStreamUrl?.let { url ->
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isBuffering = true,
                    errorMessage = null
                )
                
                _player?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(url))
                    player.prepare()
                    player.play()
                    PlayerErrorHandler.logStreamInfo(url, "RETRYING")
                }
            }
        }
    }

    fun savePlaybackState() {
        _player?.let {
            savedPlaybackPosition = it.currentPosition
            savedPlayWhenReady = it.playWhenReady
            Log.d(TAG, "Playback state saved: position=$savedPlaybackPosition, playWhenReady=$savedPlayWhenReady")
        }
    }

    private fun saveToRecentlyWatched() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (currentStreamUrl != null && state.channelName.isNotEmpty() && state.categoryUrl.isNotEmpty()) {
                    recentlyWatchedRepository.saveRecentlyWatched(
                        streamUrl = currentStreamUrl!!,
                        channelName = state.channelName,
                        channelLogo = state.channelLogoUrl,
                        category = state.category,
                        categoryUrl = state.categoryUrl
                    )
                    Log.d(TAG, "Saved to recently watched: ${state.channelName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save recently watched", e)
            }
        }
    }
    
    // Channel Navigation Functions
    private fun hasNextChannel(): Boolean {
        return channelList.isNotEmpty() && currentChannelIndex >= 0 && currentChannelIndex < channelList.size - 1
    }
    
    private fun hasPreviousChannel(): Boolean {
        return channelList.isNotEmpty() && currentChannelIndex > 0
    }
    
    fun switchToNextChannel(): com.ravi.mylivetv.domain.model.Channel? {
        if (!hasNextChannel()) {
            Log.d(TAG, "No next channel available")
            return null
        }
        
        currentChannelIndex++
        val nextChannel = channelList[currentChannelIndex]
        Log.d(TAG, "Switching to next channel: ${nextChannel.name} (index: $currentChannelIndex)")
        
        // Switch the stream
        switchChannel(nextChannel)
        return nextChannel
    }
    
    fun switchToPreviousChannel(): com.ravi.mylivetv.domain.model.Channel? {
        if (!hasPreviousChannel()) {
            Log.d(TAG, "No previous channel available")
            return null
        }
        
        currentChannelIndex--
        val prevChannel = channelList[currentChannelIndex]
        Log.d(TAG, "Switching to previous channel: ${prevChannel.name} (index: $currentChannelIndex)")
        
        // Switch the stream
        switchChannel(prevChannel)
        return prevChannel
    }
    
    private fun switchChannel(channel: com.ravi.mylivetv.domain.model.Channel) {
        // Release current player
        if (_player != null) {
            _player?.stop()
            _player?.clearMediaItems()
        }
        
        // Reset initialization flag to allow re-initialization
        isPlayerInitialized = false
        currentStreamUrl = null
        
        // Initialize with new channel
        initializePlayer(
            streamUrl = channel.streamUrl,
            channelName = channel.name,
            logoUrl = channel.logo,
            category = channel.category,
            categoryUrl = _uiState.value.categoryUrl,
            channelIndex = currentChannelIndex
        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, releasing player")
        releasePlayer()
    }
}

