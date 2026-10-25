package com.example.serverapi.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.serverSettingsDataStore by preferencesDataStore("server_settings")

data class AppSettings(
    val baseUrl: String = "http://127.0.0.1:8080/",
    val apiKey: String = "",
    val refreshSeconds: Int = 15,
    val https: Boolean = false,
    val notifications: Boolean = true,
    val darkMode: Boolean = false
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val apiKey = stringPreferencesKey("api_key")
        val refreshSeconds = intPreferencesKey("refresh_seconds")
        val https = booleanPreferencesKey("https")
        val notifications = booleanPreferencesKey("notifications")
        val darkMode = booleanPreferencesKey("dark_mode")
    }

    val settings: Flow<AppSettings> = context.serverSettingsDataStore.data.map { p ->
        AppSettings(
            baseUrl = p[Keys.baseUrl] ?: "http://127.0.0.1:8080/",
            apiKey = p[Keys.apiKey] ?: "",
            refreshSeconds = p[Keys.refreshSeconds] ?: 15,
            https = p[Keys.https] ?: false,
            notifications = p[Keys.notifications] ?: true,
            darkMode = p[Keys.darkMode] ?: false
        )
    }

    suspend fun save(value: AppSettings) {
        context.serverSettingsDataStore.edit { p ->
            p[Keys.baseUrl] = value.baseUrl.ensureTrailingSlash()
            p[Keys.apiKey] = value.apiKey
            p[Keys.refreshSeconds] = value.refreshSeconds.coerceIn(5, 300)
            p[Keys.https] = value.https
            p[Keys.notifications] = value.notifications
            p[Keys.darkMode] = value.darkMode
        }
    }
}

fun String.ensureTrailingSlash(): String = if (endsWith('/')) this else "$this/"
