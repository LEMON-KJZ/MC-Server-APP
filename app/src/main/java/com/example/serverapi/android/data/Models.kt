package com.example.serverapi.android.data

import com.google.gson.annotations.SerializedName

data class HealthResponse(val status: String = "unknown")
data class StatusResponse(
    val minecraftVersion: String = "-", val fabricVersion: String = "-", val uptimeMillis: Long = 0,
    val memoryUsedBytes: Long = 0, val memoryMaxBytes: Long = 0, val tps: Double? = null,
    val mspt: Double? = null, val onlinePlayers: Int = 0, val maxPlayers: Int = 0, val running: Boolean = false
)
data class Player(
    val name: String = "", val uuid: String = "", val dimension: String = "", val x: Double = 0.0,
    val y: Double = 0.0, val z: Double = 0.0, val health: Float = 0f, val hunger: Int = 0,
    val gameMode: String = "unknown", val latency: Int = 0, val level: Int = 0
)
data class CommandRequest(val command: String)
data class MessageRequest(val message: String)
data class GameRuleUpdate(val value: String)
data class CommandResponse(val success: Boolean = false, val resultCode: Int = 0, val feedback: String = "", val durationMs: Long = 0)
data class ApiError(val error: String? = null, val message: String? = null)
