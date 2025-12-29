package com.ravi.mylivetv.utils

import android.util.Log
import androidx.media3.common.PlaybackException

object PlayerErrorHandler {
    
    private const val TAG = "Player"
    
    fun getErrorMessage(error: PlaybackException): String {
        val message = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> 
                "Network connection failed. Please check your internet connection."
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> 
                "Connection timeout. The server is not responding."
            PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> 
                "Invalid content type. The stream format is not supported."
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> 
                "Bad HTTP status. The stream is not available."
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> 
                "Invalid stream format. Unable to parse the media."
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> 
                "Stream not found. The URL may be invalid."
            PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> 
                "No permission to access the stream."
            PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED -> 
                "HTTP traffic not permitted. Use HTTPS instead."
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> 
                "Decoder initialization failed. Your device may not support this format."
            PlaybackException.ERROR_CODE_DECODING_FAILED -> 
                "Decoding error. The stream may be corrupted."
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> 
                "Stream is too far behind live. Reloading..."
            else -> error.message ?: "Unknown playback error occurred"
        }
        
        logError(error, message)
        return message
    }
    
    private fun logError(error: PlaybackException, userMessage: String) {
        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "PLAYBACK ERROR DETAILS")
        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "Error Code: ${error.errorCode}")
        Log.e(TAG, "Error Type: ${getErrorType(error.errorCode)}")
        Log.e(TAG, "User Message: $userMessage")
        Log.e(TAG, "Original Message: ${error.message}")
        Log.e(TAG, "Timestamp: ${System.currentTimeMillis()}")
        
        error.cause?.let { cause ->
            Log.e(TAG, "Cause: ${cause.javaClass.simpleName} - ${cause.message}")
        }
        
        Log.e(TAG, "Stack Trace:", error)
        Log.e(TAG, "═══════════════════════════════════════")
    }
    
    private fun getErrorType(errorCode: Int): String {
        return when {
            errorCode in 1000..1999 -> "SOURCE_ERROR"
            errorCode in 2000..2999 -> "RENDERER_ERROR"
            errorCode in 3000..3999 -> "UNEXPECTED_ERROR"
            errorCode in 4000..4999 -> "REMOTE_ERROR"
            else -> "UNKNOWN_ERROR"
        }
    }
    
    fun logStreamInfo(url: String, status: String) {
        Log.d(TAG, "Stream: $status")
        Log.d(TAG, "URL: $url")
    }
    
    fun logChunkLoad(uri: String, bytes: Long) {
        Log.v(TAG, "Chunk loaded: $uri (${bytes / 1024} KB)")
    }
    
    fun logPlaybackState(state: String) {
        Log.i(TAG, "Playback State: $state")
    }
}



