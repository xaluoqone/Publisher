package com.xaluoqone.publisher.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import okio.buffer
import okio.source

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun execCmd(
    cmd: String,
    execPath:String,
    finishFlag: String,
    callback: suspend (String) -> Unit
): Unit = withContext(Dispatchers.IO) {
    val process = Runtime.getRuntime()
        .exec("cmd /k start /b $cmd",null,execPath.toPath().toFile())
    val source = process.inputStream.source().buffer()
    while (true) {
        while (true) {
            val msg = source.readUtf8Line()
            withContext(Dispatchers.Main){
                callback(msg ?: "")
            }
            if (msg?.contains(finishFlag) == true) {
                break
            }
        }
        break
    }
}