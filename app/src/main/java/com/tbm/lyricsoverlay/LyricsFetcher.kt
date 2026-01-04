package com.tbm.lyricsoverlay

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.URLEncoder

data class LyricsResponse(val lyrics: String?)

interface LyricsApi {
    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): LyricsResponse
}

object LyricsFetcher {
    private val api: LyricsApi

    init {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.lyrics.ovh/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(LyricsApi::class.java)
    }

    suspend fun fetchLyrics(artistRaw: String, titleRaw: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val artist = sanitize(artistRaw)
                val title = sanitize(titleRaw)
                val resp = api.getLyrics(artist, title)
                resp.lyrics
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun sanitize(s: String): String {
        return s.trim().ifEmpty { "unknown" }
            .replace("/", " ")
            .let { URLEncoder.encode(it, "utf-8") }
    }
}
