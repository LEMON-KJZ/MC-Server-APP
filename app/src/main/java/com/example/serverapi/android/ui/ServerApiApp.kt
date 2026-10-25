package com.example.serverapi.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.example.serverapi.android.data.AppSettings
import com.example.serverapi.android.data.ConnectionState
import com.example.serverapi.android.data.Player
import com.example.serverapi.android.data.ServerUiState
import com.example.serverapi.android.data.ServerViewModel
import com.example.serverapi.android.data.ensureTrailingSlash
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs

@Composable
fun ServerApiApp(viewModel: ServerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableIntStateOf(0) }
    val darkMode = state.settings.darkMode
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }
    }
    MaterialTheme(colorScheme = if (darkMode) DarkColors else LightColors) {
        val backdrop = rememberLayerBackdrop()
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize()
                    .layerBackdrop(backdrop)
                    .background(
                        Brush.verticalGradient(
                            if (darkMode) listOf(Color(0xFF0B1222), Color(0xFF171D35), Color(0xFF282040))
                            else listOf(Color(0xFFF8FAFF), Color(0xFFF1F5FF), Color(0xFFE9EFFF))
                        )
                    )
            )
            Scaffold(containerColor = Color.Transparent, topBar = { AppTopBar(page, state, viewModel::refresh) }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (page) {
                        0 -> HomePage(state, onOpenSettings = { page = 2 }, onRefresh = { viewModel.refresh() })
                        1 -> MiscPage(state, viewModel)
                        else -> SettingsPage(state.settings, state.actionMessage, viewModel::saveSettings)
                    }
                    Box(
                        Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        LiquidBottomTabs(
                            selectedTabIndex = { page }, onTabSelected = { page = it }, backdrop = backdrop,
                            tabsCount = 3, modifier = Modifier.fillMaxWidth()
                        ) {
                            LiquidBottomTab(onClick = { page = 0 }) { Icon(Icons.Default.Dashboard, null); Text("首页", style = MaterialTheme.typography.labelSmall) }
                            LiquidBottomTab(onClick = { page = 1 }) { Icon(Icons.Default.Build, null); Text("杂项", style = MaterialTheme.typography.labelSmall) }
                            LiquidBottomTab(onClick = { page = 2 }) { Icon(Icons.Default.Settings, null); Text("设置", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }
    }
}

private val LightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF4F46A5), onPrimary = Color.White,
    secondary = Color(0xFF006A6A), background = Color(0xFFF8FAFF), surface = Color.White,
    surfaceVariant = Color(0xFFE5E7F0), onSurfaceVariant = Color(0xFF45464F)
)

private val DarkColors = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFB9B8FF), secondary = Color(0xFF4DDADA), background = Color(0xFF0B1222), surface = Color(0xFF171D35)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(page: Int, state: ServerUiState, onRefresh: () -> Unit) {
    TopAppBar(title = { Text(listOf("服务器总览", "服务器杂项", "连接与设置")[page]) }, actions = {
        if (page == 0) IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新") }
        val connected = state.connection is ConnectionState.Connected
        FilterChip(selected = connected, onClick = {}, label = { Text(if (connected) "在线" else "未连接") }, leadingIcon = { Icon(Icons.Default.Wifi, null) })
    })
}

@Composable
private fun HomePage(state: ServerUiState, onOpenSettings: () -> Unit, onRefresh: () -> Unit) {
    AdaptiveList {
        item {
            if (state.connection is ConnectionState.NotConfigured) EmptyCard("还没有配置服务器", "在设置页填写 API 地址和 API Key 后即可开始管理。", "去设置", onOpenSettings)
            if (state.connection is ConnectionState.Error) EmptyCard("连接失败", (state.connection as ConnectionState.Error).message, "重试", onRefresh)
        }
        state.status?.let { status ->
            item { StatusCard(status = status) }
            item { MetricGrid(status) }
        }
        item { Text("在线玩家", style = MaterialTheme.typography.titleMedium) }
        if (state.players.isEmpty()) item { Text("当前没有在线玩家", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(state.players, key = { it.uuid }) { PlayerCard(it) }
        if (state.logs.isNotEmpty()) item { Text("最近日志", style = MaterialTheme.typography.titleMedium) }
        items(state.logs.takeLast(8)) { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun StatusCard(status: com.example.serverapi.android.data.StatusResponse) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .12f)), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Wifi, null, tint = Color(0xFF86EFAC)); Spacer(Modifier.width(10.dp)); Text("服务器运行中", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); Text("${status.onlinePlayers}/${status.maxPlayers}") }
            Text("Minecraft ${status.minecraftVersion} · Fabric ${status.fabricVersion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("运行时间 ${formatUptime(status.uptimeMillis)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricGrid(status: com.example.serverapi.android.data.StatusResponse) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("TPS", status.tps?.let { "%.1f".format(it) } ?: "—", Modifier.weight(1f))
        MetricCard("MSPT", status.mspt?.let { "%.1f ms".format(it) } ?: "—", Modifier.weight(1f))
        MetricCard("内存", "${status.memoryUsedBytes / 1024 / 1024} MB", Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) { Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .10f))) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleMedium) } } }

@Composable
private fun PlayerCard(player: Player) { Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .10f))) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.People, null); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(player.name, style = MaterialTheme.typography.titleMedium); Text("${player.gameMode} · ${player.dimension}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; Text("${player.health.toInt()}❤  ${player.latency}ms", style = MaterialTheme.typography.bodySmall) } } }

@Composable
private fun MiscPage(state: ServerUiState, vm: ServerViewModel) {
    var command by rememberSaveable { mutableStateOf("") }; var announce by rememberSaveable { mutableStateOf("") }; var player by rememberSaveable { mutableStateOf("") }; var message by rememberSaveable { mutableStateOf("") }; var rule by rememberSaveable { mutableStateOf("keepInventory") }; var ruleValue by rememberSaveable { mutableStateOf("true") }
    AdaptiveList {
        item { SectionCard("控制台指令", Icons.Default.Terminal) { OutlinedTextField(command, { command = it }, Modifier.fillMaxWidth(), label = { Text("say hello / list / save-all") }); Button(onClick = { if (command.isNotBlank()) vm.command(command) }, Modifier.fillMaxWidth()) { Text("执行指令") } } }
        item { SectionCard("公告", Icons.Default.Wifi) { OutlinedTextField(announce, { announce = it }, Modifier.fillMaxWidth(), label = { Text("公告内容") }); Button(onClick = { if (announce.isNotBlank()) vm.announce(announce) }, Modifier.fillMaxWidth()) { Text("发送公告") } } }
        item { SectionCard("玩家操作", Icons.Default.People) { OutlinedTextField(player, { player = it }, Modifier.fillMaxWidth(), label = { Text("玩家名称") }); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ActionButton("踢出") { vm.playerAction(player, "kick") }; ActionButton("封禁") { vm.playerAction(player, "ban") }; ActionButton("解封") { vm.playerAction(player, "pardon") } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ActionButton("加白") { vm.playerAction(player, "whitelist_add") }; ActionButton("移白") { vm.playerAction(player, "whitelist_remove") }; ActionButton("给 OP") { vm.playerAction(player, "op") }; ActionButton("撤 OP") { vm.playerAction(player, "deop") } }; OutlinedTextField(message, { message = it }, Modifier.fillMaxWidth(), label = { Text("私聊内容") }); OutlinedButton(onClick = { vm.message(player, message) }, Modifier.fillMaxWidth()) { Text("发送私聊") } } }
        item { SectionCard("游戏规则", Icons.Default.Build) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(rule, { rule = it }, Modifier.weight(1f), label = { Text("规则名") }); OutlinedTextField(ruleValue, { ruleValue = it }, Modifier.weight(1f), label = { Text("值") }) }; Button(onClick = { vm.gamerule(rule, ruleValue) }, Modifier.fillMaxWidth()) { Text("更新规则") }; Text("已读取规则：${state.gamerules.size} 项", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        state.actionMessage?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
    }
}

@Composable
private fun SettingsPage(settings: AppSettings, message: String?, save: (AppSettings) -> Unit) {
    var url by rememberSaveable(settings.baseUrl) { mutableStateOf(settings.baseUrl) }; var key by rememberSaveable(settings.apiKey) { mutableStateOf(settings.apiKey) }; var https by rememberSaveable(settings.https) { mutableStateOf(settings.https) }; var refresh by rememberSaveable(settings.refreshSeconds) { mutableIntStateOf(settings.refreshSeconds) }; var notifications by rememberSaveable(settings.notifications) { mutableStateOf(settings.notifications) }; var darkMode by rememberSaveable(settings.darkMode) { mutableStateOf(settings.darkMode) }
    val context = LocalContext.current
    AdaptiveList {
        item { SectionCard("服务器连接", Icons.Default.Link) { OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("API 地址（含端口）") }, singleLine = true); OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("API Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation()); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("HTTPS", Modifier.weight(1f)); Switch(https, { https = it }) }; Button(onClick = { save(AppSettings(baseUrl = url.ensureTrailingSlash(), apiKey = key.trim(), refreshSeconds = refresh, https = https, notifications = notifications, darkMode = darkMode)) }, Modifier.fillMaxWidth()) { Text("保存并连接") }; message?.let { Text(it, color = MaterialTheme.colorScheme.primary) } } }
        item { SectionCard("客户端设置", Icons.Default.Settings) { Text("自动刷新间隔：${refresh} 秒"); Slider(refresh.toFloat(), { refresh = it.toInt() }, valueRange = 5f..60f, steps = 10); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("事件通知", Modifier.weight(1f)); Switch(notifications, { notifications = it }) }; Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("深色模式"); Text(if (darkMode) "深色界面" else "浅色界面", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; Switch(darkMode, { darkMode = it; save(AppSettings(baseUrl = url.ensureTrailingSlash(), apiKey = key.trim(), refreshSeconds = refresh, https = https, notifications = notifications, darkMode = it)) }) } } }
        item { AboutCard { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LEMON-KJZ"))) } }
    }
}

@Composable
private fun AboutCard(openGithub: () -> Unit) { SectionCard("关于作者", Icons.Default.Link) { Row(verticalAlignment = Alignment.CenterVertically) { AsyncImage(model = "https://github.com/LEMON-KJZ.png?size=256", contentDescription = "作者头像", modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("LEMON-KJZ", style = MaterialTheme.typography.titleMedium); Text("Minecraft Server Control", color = MaterialTheme.colorScheme.onSurfaceVariant) }; TextButton(onClick = openGithub) { Text("GitHub") } }; Text("Android 客户端 · API v1", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) } }

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f)), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(title, style = MaterialTheme.typography.titleMedium) }; content() } } }

@Composable
private fun RowScope.ActionButton(label: String, onClick: () -> Unit) { OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1) } }

@Composable
private fun EmptyCard(title: String, body: String, action: String, onClick: () -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f)), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = onClick) { Text(action) } } } }

@Composable
private fun AdaptiveList(content: LazyListScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val horizontalPadding = when {
            maxWidth < 360.dp -> 12.dp
            maxWidth > 600.dp -> 28.dp
            else -> 16.dp
        }
        LazyColumn(
            Modifier.fillMaxWidth().widthIn(max = 760.dp).align(Alignment.TopCenter).padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 128.dp),
            content = content
        )
    }
}

private fun formatUptime(ms: Long): String { val total = ms / 1000; val h = total / 3600; val m = (total % 3600) / 60; return "${h}h ${m}m" }
