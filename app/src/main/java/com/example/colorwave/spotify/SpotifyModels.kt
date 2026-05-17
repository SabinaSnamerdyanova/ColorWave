package com.example.colorwave.spotify

import com.squareup.moshi.Json

data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Int
)

data class SpotifyFeatures(
    val energy: Float?,
    val valence: Float?,
    val danceability: Float?,
    val acousticness: Float?,
    val tempo: Float?,
    val loudness: Float?
)