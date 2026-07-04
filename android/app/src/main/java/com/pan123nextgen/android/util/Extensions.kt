package com.pan123nextgen.android.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun Context.copyToClipboard(text: String, label: String = "Copied") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

fun maskPhoneNumber(name: String): String {
    if (name.length == 11 && name.all { it.isDigit() }) {
        return "${name.substring(0, 3)}****${name.substring(7)}"
    }
    return name
}