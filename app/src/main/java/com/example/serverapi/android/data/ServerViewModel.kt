package com.example.serverapi.android.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface ConnectionState { data object NotConfigured : ConnectionState; data object Loading : ConnectionState; data object Connected : ConnectionState; data class Error(val message: String) : ConnectionState }
data class ServerUiState(
    val connection: ConnectionState = ConnectionState.NotConfigured,
    val settings: AppSettings = AppSettings(),
    val status: StatusResponse? = null,
    val players: List<Player> = emptyList(),
    val logs: List<String> = emptyList(),
    val gamerules: Map<String, String> = emptyMap(),
    val actionMessage: String? = null,
    val refreshing: Boolean = false
)

class ServerViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsStore = SettingsStore(app)
    private val repository = ServerRepository()
    private val _state = MutableStateFlow(ServerUiState())
    val state: StateFlow<ServerUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null
    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            settingsStore.settings.collectLatest { settings ->
                repository.configure(settings)
                _state.value = _state.value.copy(settings = settings, connection = if (settings.apiKey.isBlank()) ConnectionState.NotConfigured else _state.value.connection)
                restartAutoRefresh(settings)
            }
        }
    }

    private fun restartAutoRefresh(settings: AppSettings) {
        refreshJob?.cancel()
        if (settings.apiKey.isBlank()) return
        refreshJob = viewModelScope.launch {
            while (isActive) {
                refreshInternal(silent = false)
                delay(settings.refreshSeconds * 1000L)
            }
        }
    }

    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch { settingsStore.save(settings); _state.value = _state.value.copy(actionMessage = "设置已保存") }
    }

    fun refresh(silent: Boolean = false) {
        if (_state.value.settings.apiKey.isBlank()) return
        viewModelScope.launch { refreshInternal(silent) }
    }

    private suspend fun refreshInternal(silent: Boolean) = refreshMutex.withLock {
        if (!silent) _state.value = _state.value.copy(connection = ConnectionState.Loading, refreshing = true)
        try {
            coroutineScope {
                val status = async { repository.status() }
                val players = async { repository.players() }
                val logs = async { runCatching { repository.logs() }.getOrDefault(emptyList()) }
                val gamerules = async { runCatching { repository.gamerules() }.getOrDefault(emptyMap()) }
                _state.value = _state.value.copy(
                    connection = ConnectionState.Connected,
                    status = status.await(), players = players.await(),
                    logs = logs.await(), gamerules = gamerules.await(), refreshing = false
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(connection = ConnectionState.Error(e.message ?: "连接失败"), refreshing = false)
        }
    }

    fun command(command: String) = runAction { repository.command(command).feedback.ifBlank { "指令已执行" } }
    fun announce(message: String) = runAction { repository.announce(message).feedback.ifBlank { "公告已发送" } }
    fun playerAction(name: String, action: String) = runAction { repository.playerAction(name, action).feedback.ifBlank { "操作已完成" } }
    fun message(name: String, text: String) = runAction { repository.message(name, text).feedback.ifBlank { "私聊已发送" } }
    fun gamerule(name: String, value: String) = runAction { repository.gamerule(name, value).feedback.ifBlank { "游戏规则已更新" } }

    private fun runAction(action: suspend () -> String) {
        viewModelScope.launch {
            try { _state.value = _state.value.copy(actionMessage = action()); refreshInternal(silent = true) }
            catch (e: Exception) { _state.value = _state.value.copy(actionMessage = e.message ?: "操作失败") }
        }
    }
}
