package com.xaluoqone.publisher.ext

import com.xaluoqone.publisher.utils.toJson
import okio.Buffer
import okio.BufferedSource
import java.nio.charset.Charset

fun BufferedSource.readLine(charset: Charset = Charsets.UTF_8): String {
    return when (val newline = indexOf('\n'.code.toByte())) {
        -1L -> {
            if (buffer.size != 0L) {
                readString(buffer.size, charset)
            } else {
                ""
            }
        }
        else -> {
            buffer.readLine(newline, charset)
        }
    }
}

fun Buffer.readLine(newline: Long, charset: Charset): String {
    return when {
        newline > 0 && this[newline - 1] == '\r'.code.toByte() -> {
            // Read everything until '\r\n', then skip the '\r\n'.
            val result = readString(newline - 1L, charset)
            skip(2L)
            result
        }
        else -> {
            // Read everything until '\n', then skip the '\n'.
            val result = readString(newline, charset)
            skip(1L)
            result
        }
    }
}

fun BufferedSource.easyRead(size: Int = 1024): String {
    val array = ByteArray(size)
    var res = ""
    var read = read(array)
    while (read != -1) {
        val bytes = array.toMutableList().subList(0, read)
        val byteJson = bytes.toJson()
        var fixBytes = bytes
        if (byteJson.startsWith("[27,91,50,74")
            || byteJson.startsWith("[27,91,48,102")
        ) {
            fixBytes = fixBytes.subList(3, fixBytes.lastIndex)
        }
        if (fixBytes.isNotEmpty()) {
            res += fixBytes.toByteArray().decodeToString()
        }
        if (read < 1024) {
            break
        }
        read = read(array)
    }
    return res
}