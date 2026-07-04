package com.pan123nextgen.android.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pan123nextgen.android.data.ConfigManager
import com.pan123nextgen.android.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val configManager = remember { ConfigManager.getInstance() }
    val settings = remember { configManager.getSettings() }

    var downloadPath by remember { mutableStateOf(settings.defaultDownloadPath) }
    var askLocation by remember { mutableStateOf(settings.askDownloadLocation) }
    var multiThread by remember { mutableStateOf(settings.multiThreadDownload) }
    var downloadSpeed by remember { mutableIntStateOf(settings.downloadSpeedLimit) }
    var uploadSpeed by remember { mutableIntStateOf(settings.uploadSpeedLimit) }
    var proxyEnabled by remember { mutableStateOf(settings.proxyEnabled) }
    var proxyType by remember { mutableStateOf(settings.proxyType) }
    var proxyHost by remember { mutableStateOf(settings.proxyHost) }
    var proxyPort by remember { mutableStateOf(settings.proxyPort.toString()) }
    var proxyUser by remember { mutableStateOf(settings.proxyUsername) }
    var proxyPass by remember { mutableStateOf(settings.proxyPassword) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        // Download settings
        SettingsGroup("下载设置") {
            SettingsSwitchItem(
                icon = Icons.Filled.Download,
                title = "每次询问下载位置",
                subtitle = "下载文件时是否每次都询问保存位置",
                checked = askLocation,
                onCheckedChange = {
                    askLocation = it
                    settings.askDownloadLocation = it
                    configManager.saveSettings(settings)
                }
            )

            SettingsSwitchItem(
                icon = Icons.Filled.Sync,
                title = "多线程下载",
                subtitle = "启用多线程分片下载以提升下载速度",
                checked = multiThread,
                onCheckedChange = {
                    multiThread = it
                    settings.multiThreadDownload = it
                    configManager.saveSettings(settings)
                }
            )

            SettingsSpeedItem(
                icon = Icons.Filled.Speed,
                title = "下载限速",
                subtitle = "限制下载速度，0 表示不限制",
                value = downloadSpeed,
                onValueChange = {
                    downloadSpeed = it
                    settings.downloadSpeedLimit = it
                    configManager.saveSettings(settings)
                }
            )

            SettingsSpeedItem(
                icon = Icons.Filled.Speed,
                title = "上传限速",
                subtitle = "限制上传速度，0 表示不限制",
                value = uploadSpeed,
                onValueChange = {
                    uploadSpeed = it
                    settings.uploadSpeedLimit = it
                    configManager.saveSettings(settings)
                }
            )
        }

        // Proxy settings
        SettingsGroup("网络代理") {
            SettingsSwitchItem(
                icon = Icons.Filled.Public,
                title = "启用代理",
                subtitle = "开启后所有网络请求将通过代理服务器",
                checked = proxyEnabled,
                onCheckedChange = {
                    proxyEnabled = it
                    settings.proxyEnabled = it
                    configManager.saveSettings(settings)
                }
            )

            if (proxyEnabled) {
                SettingsTextFieldItem(
                    icon = Icons.Filled.Public,
                    title = "代理主机",
                    subtitle = "代理服务器地址",
                    value = proxyHost,
                    onValueChange = {
                        proxyHost = it
                        settings.proxyHost = it
                        configManager.saveSettings(settings)
                    },
                    placeholder = "127.0.0.1"
                )

                SettingsTextFieldItem(
                    icon = Icons.Filled.Public,
                    title = "代理端口",
                    subtitle = "代理服务器端口",
                    value = proxyPort,
                    onValueChange = {
                        proxyPort = it
                        settings.proxyPort = it.toIntOrNull() ?: 0
                        configManager.saveSettings(settings)
                    },
                    placeholder = "8080"
                )

                SettingsTextFieldItem(
                    icon = Icons.Filled.Person,
                    title = "代理用户名",
                    subtitle = "代理认证用户名（可选）",
                    value = proxyUser,
                    onValueChange = {
                        proxyUser = it
                        settings.proxyUsername = it
                        configManager.saveSettings(settings)
                    },
                    placeholder = ""
                )

                SettingsTextFieldItem(
                    icon = Icons.Filled.Person,
                    title = "代理密码",
                    subtitle = "代理认证密码（可选）",
                    value = proxyPass,
                    onValueChange = {
                        proxyPass = it
                        settings.proxyPassword = it
                        configManager.saveSettings(settings)
                    },
                    placeholder = "",
                    isPassword = true
                )
            }
        }

        // About
        SettingsGroup("关于") {
            SettingsInfoItem(
                icon = Icons.Filled.Info,
                title = "关于",
                subtitle = "123panNextGen v${Constants.VERSION} © Copyright ${Constants.YEAR}",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_URL))
                    context.startActivity(intent)
                }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
}

@Composable
fun SettingsSpeedItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = !showPicker }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = if (value == 0) "不限制" else "${value} KB/s",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (showPicker) {
        SpeedPickerDialog(
            currentSpeed = value,
            onDismiss = { showPicker = false },
            onConfirm = { onValueChange(it) }
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
}

@Composable
fun SpeedPickerDialog(currentSpeed: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var speed by remember { mutableIntStateOf(currentSpeed) }
    val presets = listOf(0, 100, 500, 1024, 2048, 5120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("速度限制") },
        text = {
            Column {
                // Preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = speed == preset,
                            onClick = { speed = preset },
                            label = {
                                Text(
                                    if (preset == 0) "不限" else "${preset}KB/s",
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("当前值: ${if (speed == 0) "不限制" else "${speed} KB/s"}", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(speed) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun SettingsTextFieldItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPassword) PasswordVisualTransformation()
                    else VisualTransformation.None
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
}

@Composable
fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}