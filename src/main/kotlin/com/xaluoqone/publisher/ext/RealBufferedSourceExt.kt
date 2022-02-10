package com.xaluoqone.publisher.ext

import com.xaluoqone.publisher.utils.toJson
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
        val bytes = array.toMutableList().subList(0, read)
        val byteJson = bytes.toJson()
        var fixBytes = bytes
        if (byteJson.startsWith("[27,91,50,74")
            || byteJson.startsWith("[27,91,48,102")
        ) {
            fixBytes = fixBytes.subList(3, fixBytes.lastIndex)
        } else if (byteJson.startsWith("[27,91,48,71,32,32,27,91,57,54,109")) {
            fixBytes = if (read > 48) {
                fixBytes.subList(0, 45)
            } else {
                fixBytes.subList(0, 30)
            }
            fixBytes = fixBytes.apply {
                (12..17).forEach { _ ->
                    removeAt(12)
                }
                repeat(11) {
                    removeFirst()
                }
            }
        } else if (byteJson.startsWith("[-24,-81,-69,-27,-113,-106,-28,-72,-102,-27,-118,-95,-27,-73,-91,-25,-88,-117,-26,-106,-121,-28,-69,-74,")) {
            fixBytes = fixBytes.subList(0, fixBytes.lastIndexOf(37) + 1)
            (25..62).forEach { _ ->
                fixBytes.removeAt(25)
            }
        } else if (byteJson.startsWith("[27,91,48,71,27,91,50,75")) {
            fixBytes = fixBytes.subList(7, fixBytes.lastIndex)
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