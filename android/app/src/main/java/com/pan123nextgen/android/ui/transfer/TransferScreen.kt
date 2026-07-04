package com.pan123nextgen.android.ui.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pan123nextgen.android.api.TaskStatus
import com.pan123nextgen.android.api.TaskType
import com.pan123nextgen.android.api.TransferTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("上传", "下载")
    val tasks = remember { mutableStateListOf<TransferTask>() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "传输管理",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        // Tab row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (index == 0) Icons.Filled.Upload else Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(title)
                        }
                    }
                )
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("暂无传输任务", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tasks.filter {
                    if (selectedTab == 0) it.type == TaskType.UPLOAD else it.type == TaskType.DOWNLOAD
                }, key = { it.id }) { task ->
                    TransferTaskItem(task = task)
                }
            }
        }
    }
}

@Composable
fun TransferTaskItem(task: TransferTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                // Status
                val statusColor = when (task.status) {
                    TaskStatus.COMPLETED -> Color(0xFF107C10)
                    TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                    TaskStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                }
                Text(
                    text = when (task.status) {
                        TaskStatus.WAITING -> "等待中"
                        TaskStatus.UPLOADING -> "上传中"
                        TaskStatus.DOWNLOADING -> "下载中"
                        TaskStatus.COMPLETED -> "已完成"
                        TaskStatus.FAILED -> "失败"
                        TaskStatus.CANCELLED -> "已取消"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            // Percentage
            Text(
                text = "${task.progress}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}