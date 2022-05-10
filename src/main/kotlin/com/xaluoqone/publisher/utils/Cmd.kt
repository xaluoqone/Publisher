package com.xaluoqone.publisher.utils

import com.xaluoqone.publisher.ext.easyRead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import okio.buffer
import okio.source

fun CoroutineScope.execCmd(
    cmd: Array<String>,
    execPath: String,
    onRead: (String) -> Unit
) {
    val process = ProcessBuilder()
        .redirectErrorStream(true)
        .directory(execPath.toPath().toFile())
        .command(*cmd)
        .start()
    var flag = true
    val job = launch {
        process.doOnExit {
            println("execCmd 执行结束")
            // 避免在未读完控制台输出时就将流关闭
            delay(1000)
            flag = false
            inputStream.close()
            outputStream.close()
            errorStream.close()
            destroy()
        }
    }
    val source = process.inputStream.source().buffer()
    while (flag) {
        val msg = source.easyRead()
        if (msg.isNotEmpty()) {
            onRead(msg)
        }
    }
    job.cancel()
}

inline fun Process.doOnExit(exit: Process.() -> Unit) {
    val res = waitFor()
    if (res == 0) {
        exit()
    }
}