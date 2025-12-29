package com.ravi.mylivetv.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject

class PlaylistService @Inject constructor(
    private val client: HttpClient
) {

    suspend fun fetchPlaylist(url: String): String {
        return try {
            val response = client.get(url)
            response.bodyAsText()
        } catch (e: Exception) {
            throw RuntimeException("Network error while fetching playlist", e)
        }
    }
}
