package com.xaluoqone.publisher.ext

import okio.Buffer
import okio.BufferedSource
import java.nio.charset.Charset

inline fun BufferedSource.readLine(charset: Charset = Charsets.UTF_8): String {
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

inline fun BufferedSource.easyRead(size: Int = 1024): String {
    val array = ByteArray(size)
    var res = ""
    var read = read(array)
    while (read != -1) {
        res += String(array, 0, read)
        if (read < 1024) {
            break
        }
        read = read(array)
    }
    return res
}