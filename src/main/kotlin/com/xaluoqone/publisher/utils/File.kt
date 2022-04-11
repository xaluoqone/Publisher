package com.xaluoqone.publisher.utils

import androidx.compose.ui.awt.ComposeWindow
import okio.FileSystem
import okio.Path.Companion.toPath
import java.awt.FileDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun openFileDialog(isFolder: Boolean) = suspendCoroutine<String?> {
    System.setProperty("apple.awt.fileDialogForDirectories", isFolder.toString())
    val dialog = FileDialog(ComposeWindow())
    dialog.isVisible = true
    it.resume(if (dialog.file != null) "${dialog.directory}${dialog.file}" else null)
}

fun readFile(path: String): String {
    return FileSystem.SYSTEM.read(path.toPath()) {
        readUtf8()
    }
}

fun writeFile(path: String, content: String) {
    FileSystem.SYSTEM.write(path.toPath()) {
        writeUtf8(content)
    }
}