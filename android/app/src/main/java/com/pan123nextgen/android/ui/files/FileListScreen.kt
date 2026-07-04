package com.pan123nextgen.android.ui.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pan123nextgen.android.api.FileItemModel
import com.pan123nextgen.android.api.Pan123Api
import com.pan123nextgen.android.data.ConfigManager
import com.pan123nextgen.android.ui.login.Pan123ApiHolder
import com.pan123nextgen.android.ui.theme.WinUIColor
import com.pan123nextgen.android.util.copyToClipboard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { Pan123ApiHolder.instance ?: Pan123Api() }

    var files by remember { mutableStateOf(listOf<FileItemModel>()) }
    var currentDirId by remember { mutableStateOf(api.parentFileId) }
    var isLoading by remember { mutableStateOf(true) }
    var pathStack by remember { mutableStateOf(listOf(0L to "根目录")) }
    var sortMode by remember { mutableIntStateOf(0) }
    var sortAsc by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FileItemModel?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var sharePassword by remember { mutableStateOf("") }

    // Load file list
    fun loadFiles() {
        isLoading = true
        scope.launch {
            val result = api.loadDirectory(currentDirId, all = true)
            if (result.first == 0) {
                files = result.second
            }
            isLoading = false
        }
    }

    LaunchedEffect(currentDirId) { loadFiles() }

    // Compute sorted files
    val sortedFiles = remember(files, sortMode, sortAsc) {
        val folders = files.filter { it.isDir() }
        val fileItems = files.filter { !it.isDir() }

        val sortedFolders = when {
            sortMode == 2 -> if (sortAsc) folders.sortedBy { it.size } else folders.sortedByDescending { it.size }
            else -> if (sortAsc) folders else folders.reversed()
        }
        val sortedFiles = when {
            sortMode == 2 -> if (sortAsc) fileItems.sortedBy { it.size } else fileItems.sortedByDescending { it.size }
            else -> if (sortAsc) fileItems else fileItems.reversed()
        }
        sortedFolders + sortedFiles
    }

    // Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                scope.launch {
                    api.createFolder(name, currentDirId).onSuccess {
                        showCreateFolderDialog = false
                        loadFiles()
                    }
                }
            }
        )
    }

    if (showRenameDialog && selectedFile != null) {
        RenameDialog(
            currentName = selectedFile!!.fileName,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                scope.launch {
                    api.renameFile(selectedFile!!.fileId, newName).onSuccess {
                        showRenameDialog = false
                        loadFiles()
                    }
                }
            }
        )
    }

    if (showDeleteConfirm && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${selectedFile!!.fileName}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        api.trashFile(selectedFile!!.fileId).onSuccess {
                            showDeleteConfirm = false
                            loadFiles()
                        }
                    }
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showShareDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("分享文件") },
            text = {
                Column {
                    Text("为「${selectedFile!!.fileName}」生成分享链接")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sharePassword,
                        onValueChange = { sharePassword = it },
                        label = { Text("分享密码（可选）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        api.shareFile(listOf(selectedFile!!.fileId), sharePassword).onSuccess { url ->
                            context.copyToClipboard(url, "分享链接")
                            showShareDialog = false
                        }
                    }
                }) { Text("生成链接") }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) { Text("取消") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with breadcrumb and actions
        FileListTopBar(
            pathStack = pathStack,
            onBack = {
                if (pathStack.size > 1) {
                    pathStack = pathStack.dropLast(1)
                    currentDirId = pathStack.last().first
                }
            },
            onNavigateTo = { index ->
                pathStack = pathStack.take(index + 1)
                currentDirId = pathStack.last().first
            },
            onRefresh = { loadFiles() },
            onCreateFolder = { showCreateFolderDialog = true },
            onSortModeChange = { mode ->
                if (sortMode == mode) sortAsc = !sortAsc
                else {
                    sortMode = mode
                    sortAsc = mode == 0
                }
            },
            sortMode = sortMode,
            sortAsc = sortAsc
        )

        HorizontalDivider()

        // File list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (sortedFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("此文件夹为空", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedFiles, key = { it.fileId }) { file ->
                    FileListItem(
                        file = file,
                        onClick = {
                            if (file.isDir()) {
                                pathStack = pathStack + (file.fileId to file.fileName)
                                currentDirId = file.fileId
                            }
                        },
                        onLongClick = {
                            selectedFile = file
                            showMenu = true
                        },
                        onMenuClick = { action ->
                            selectedFile = file
                            when (action) {
                                MenuAction.RENAME -> showRenameDialog = true
                                MenuAction.DELETE -> showDeleteConfirm = true
                                MenuAction.SHARE -> showShareDialog = true
                                MenuAction.COPY_LINK -> {
                                    scope.launch {
                                        api.getDownloadUrl(file.fileId, file.etag, file.s3keyFlag, file.type, file.fileName, file.size)
                                            .onSuccess { url -> context.copyToClipboard(url, "下载链接") }
                                    }
                                }
                                MenuAction.DOWNLOAD -> {
                                    scope.launch {
                                        api.getDownloadUrl(file.fileId, file.etag, file.s3keyFlag, file.type, file.fileName, file.size)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Context menu
    if (showMenu && selectedFile != null) {
        val file = selectedFile!!
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(file.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column {
                    FileMenuItem(Icons.Filled.Download, "下载") {
                        showMenu = false
                        scope.launch {
                            api.getDownloadUrl(file.fileId, file.etag, file.s3keyFlag, file.type, file.fileName, file.size)
                        }
                    }
                    FileMenuItem(Icons.Filled.Link, "复制下载链接") {
                        showMenu = false
                        scope.launch {
                            api.getDownloadUrl(file.fileId, file.etag, file.s3keyFlag, file.type, file.fileName, file.size)
                                .onSuccess { url -> context.copyToClipboard(url) }
                        }
                    }
                    FileMenuItem(Icons.Filled.Share, "分享") { showMenu = false; showShareDialog = true }
                    FileMenuItem(Icons.Filled.Edit, "重命名") { showMenu = false; showRenameDialog = true }
                    FileMenuItem(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error) {
                        showMenu = false; showDeleteConfirm = true
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showMenu = false }) { Text("取消") } }
        )
    }
}

data class BreadcrumbItem(val id: Long, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListTopBar(
    pathStack: List<Pair<Long, String>>,
    onBack: () -> Unit,
    onNavigateTo: (Int) -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onSortModeChange: (Int) -> Unit,
    sortMode: Int,
    sortAsc: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button + Breadcrumb
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (pathStack.size > 1) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }

                // Breadcrumb - simplified
                Text(
                    text = pathStack.lastOrNull()?.second ?: "根目录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Action buttons
            Row {
                IconButton(onClick = onCreateFolder) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = "新建文件夹")
                }
                IconButton(onClick = { onSortModeChange(if (sortMode == 0) 2 else 0) }) {
                    Icon(
                        if (sortMode == 2) Icons.Filled.SortByAlpha else Icons.Filled.Sort,
                        contentDescription = "排序"
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                }
            }
        }

        // Breadcrumb trail
        if (pathStack.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pathStack.forEachIndexed { index, (id, name) ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index == pathStack.lastIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable { onNavigateTo(index) }
                            .padding(horizontal = 2.dp)
                    )
                    if (index < pathStack.lastIndex) {
                        Text(
                            " > ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileItemModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: (MenuAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = if (file.isDir()) Icons.Filled.Folder else getFileIcon(file.fileName),
                contentDescription = null,
                tint = if (file.isDir()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 12.dp)
            )

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = if (file.isDir()) "文件夹" else file.formattedSize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // File size (for files)
            if (!file.isDir()) {
                Text(
                    text = file.formattedSize(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // More button
            IconButton(onClick = onLongClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = "更多", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FileMenuItem(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = tint, modifier = Modifier.weight(1f))
    }
}

enum class MenuAction { RENAME, DELETE, SHARE, COPY_LINK, DOWNLOAD }

fun getFileIcon(fileName: String): ImageVector {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Icons.Filled.Image
        "mp4", "avi", "mkv", "mov", "wmv", "flv" -> Icons.Filled.VideoFile
        "mp3", "wav", "flac", "aac", "ogg" -> Icons.Filled.AudioFile
        "doc", "docx", "pdf", "txt" -> Icons.Filled.Description
        "zip", "rar", "7z", "tar", "gz" -> Icons.Filled.FolderZip
        "xls", "xlsx" -> Icons.Filled.TableChart
        "ppt", "pptx" -> Icons.Filled.Slideshow
        else -> Icons.Filled.InsertDriveFile
    }
}

@Composable
fun CreateFolderDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var folderName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("文件夹名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (folderName.isNotBlank()) onCreate(folderName.trim()) }) {
                Text("创建")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun RenameDialog(currentName: String, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var newName by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("新名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (newName.isNotBlank() && newName != currentName) onRename(newName.trim()) }) {
                Text("确定")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}