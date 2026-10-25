package com.example.serverapi.android.data

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServerRepository {
    private var service: ServerApiService? = null
    private var configuredUrl: String = ""
    private var configuredKey: String = ""

    fun configure(settings: AppSettings) {
        val url = settings.baseUrl.trim().ensureTrailingSlash()
        if (service == null || url != configuredUrl || settings.apiKey != configuredKey) {
            configuredUrl = url; configuredKey = settings.apiKey
            val auth = Interceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    if (configuredKey.isNotBlank()) header("Authorization", "Bearer $configuredKey")
                }.build()
                chain.proceed(request)
            }
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.NONE }
            val client = OkHttpClient.Builder()
                .addInterceptor(auth).addInterceptor(logging)
                .connectTimeout(8, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
            service = Retrofit.Builder().baseUrl(url).client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create())).build()
                .create(ServerApiService::class.java)
        }
    }

    suspend fun health() = withContext(Dispatchers.IO) { service!!.health() }
    suspend fun status() = withContext(Dispatchers.IO) { service!!.status() }
    suspend fun players() = withContext(Dispatchers.IO) { service!!.players() }
    suspend fun logs() = withContext(Dispatchers.IO) { service!!.logs() }
    suspend fun gamerules() = withContext(Dispatchers.IO) { service!!.gamerules() }
    suspend fun command(command: String) = withContext(Dispatchers.IO) { service!!.command(CommandRequest(command)) }
    suspend fun announce(message: String) = withContext(Dispatchers.IO) { service!!.announce(MessageRequest(message)) }
    suspend fun playerAction(name: String, action: String) = withContext(Dispatchers.IO) {
        when (action) {
            "kick" -> service!!.kick(name); "ban" -> service!!.ban(name); "pardon" -> service!!.pardon(name)
            "whitelist_add" -> service!!.whitelistAdd(name); "whitelist_remove" -> service!!.whitelistRemove(name)
            "op" -> service!!.op(name); "deop" -> service!!.deop(name)
            else -> error("Unsupported action")
        }
    }
    suspend fun message(name: String, text: String) = withContext(Dispatchers.IO) { service!!.message(name, MessageRequest(text)) }
    suspend fun gamerule(name: String, value: String) = withContext(Dispatchers.IO) { service!!.gamerule(name, GameRuleUpdate(value)) }
}
