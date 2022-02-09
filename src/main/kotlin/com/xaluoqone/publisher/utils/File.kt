package com.xaluoqone.publisher.utils

import okio.FileSystem
import okio.Path.Companion.toPath

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