package com.xaluoqone.publisher.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
    return@withContext FileSystem.SYSTEM.read(path.toPath()) {
        readUtf8()
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun writeFile(path: String, content: String) = withContext(Dispatchers.IO) {
    FileSystem.SYSTEM.write(path.toPath()) {
        writeUtf8(content)
    }
}