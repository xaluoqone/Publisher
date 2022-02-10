package com.xaluoqone.publisher.utils

import com.xaluoqone.publisher.ext.easyRead
import okio.Path.Companion.toPath
import okio.buffer
import okio.source

fun execCmd(
    cmd: Array<String>,
    execPath: String,
    finishFlag: String,
    callback: (String) -> Unit
) {
    val process = ProcessBuilder()
        .redirectErrorStream(true)
        .directory(execPath.toPath().toFile())
        .command("cmd", "/c", *cmd)
        .start()
    val source = process.inputStream.source().buffer()
    while (true) {
        val msg = source.easyRead()
        callback(msg)
        if (msg.contains(finishFlag)) {
            source.close()
            break
        }
    }
}