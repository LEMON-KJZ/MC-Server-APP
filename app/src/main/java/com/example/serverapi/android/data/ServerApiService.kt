package com.example.serverapi.android.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ServerApiService {
    @GET("api/v1/health") suspend fun health(): HealthResponse
    @GET("api/v1/status") suspend fun status(): StatusResponse
    @GET("api/v1/players") suspend fun players(): List<Player>
    @POST("api/v1/command") suspend fun command(@Body request: CommandRequest): CommandResponse
    @POST("api/v1/announce") suspend fun announce(@Body request: MessageRequest): CommandResponse
    @POST("api/v1/players/{name}/kick") suspend fun kick(@Path("name") name: String): CommandResponse
    @POST("api/v1/players/{name}/ban") suspend fun ban(@Path("name") name: String): CommandResponse
    @POST("api/v1/players/{name}/pardon") suspend fun pardon(@Path("name") name: String): CommandResponse
    @POST("api/v1/players/{name}/whitelist") suspend fun whitelistAdd(@Path("name") name: String): CommandResponse
    @DELETE("api/v1/players/{name}/whitelist") suspend fun whitelistRemove(@Path("name") name: String): CommandResponse
    @POST("api/v1/players/{name}/op") suspend fun op(@Path("name") name: String): CommandResponse
    @DELETE("api/v1/players/{name}/op") suspend fun deop(@Path("name") name: String): CommandResponse
    @POST("api/v1/players/{name}/message") suspend fun message(@Path("name") name: String, @Body request: MessageRequest): CommandResponse
    @GET("api/v1/logs") suspend fun logs(): List<String>
    @GET("api/v1/gamerules") suspend fun gamerules(): Map<String, String>
    @PUT("api/v1/gamerules/{name}") suspend fun gamerule(@Path("name") name: String, @Body request: GameRuleUpdate): CommandResponse
}
