package com.example.colorwave.spotify

import android.util.Base64
import com.example.colorwave.audio.AudioFeatures
import com.example.colorwave.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SpotifyService {

    private val tokenApi = Retrofit.Builder()
        .baseUrl(SpotifyConfig.TOKEN_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SpotifyApi::class.java)

    private val api = Retrofit.Builder()
        .baseUrl(SpotifyConfig.API_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SpotifyApi::class.java)

    private var cachedToken: String? = null

    private suspend fun getAccessToken(): String {
        if (cachedToken != null) return cachedToken!!

        val creds = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
        val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)

        val response = tokenApi.getToken(auth)
        cachedToken = response.accessToken
        return response.accessToken
    }

    suspend fun loadFeatures(trackId: String, seed: Long): AudioFeatures {
        val token = getAccessToken()
        val f = api.getAudioFeatures("Bearer $token", trackId)

        return AudioFeatures(
            energy = f.energy ?: 0.5f,
            brightness = f.danceability ?: 0.5f,
            complexity = 1f - (f.acousticness ?: 0.5f),
            valence = f.valence ?: 0.5f,
            seed = seed
        )
    }
}