package com.example.colorwave.spotify

import retrofit2.http.*

interface SpotifyApi {

    @POST("api/token")
    @FormUrlEncoded
    suspend fun getToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): TokenResponse

    @GET("v1/audio-features/{id}")
    suspend fun getAudioFeatures(
        @Header("Authorization") bearer: String,
        @Path("id") trackId: String
    ): SpotifyFeatures
}