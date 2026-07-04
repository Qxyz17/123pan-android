package com.pan123nextgen.android.util

import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    fun getFileExtension(filename: String): String {
        val dot = filename.lastIndexOf('.')
        return if (dot >= 0) filename.substring(dot).lowercase() else ""
    }

    fun isImageFile(ext: String): Boolean = ext in listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
    fun isVideoFile(ext: String): Boolean = ext in listOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv")
    fun isAudioFile(ext: String): Boolean = ext in listOf(".mp3", ".wav", ".flac", ".aac", ".ogg")
    fun isDocFile(ext: String): Boolean = ext in listOf(".doc", ".docx", ".pdf", ".txt", ".xls", ".xlsx", ".ppt", ".pptx")
    fun isArchiveFile(ext: String): Boolean = ext in listOf(".zip", ".rar", ".7z", ".tar", ".gz")
}