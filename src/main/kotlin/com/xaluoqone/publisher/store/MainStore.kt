package com.xaluoqone.publisher.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xaluoqone.publisher.utils.readFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainStore {
    var state by mutableStateOf(initialState())
        private set

    private fun initialState() = MainState(
        "/Users/feifanluo/Projects/Unified/UnifiedRN/ldv-iotapp-base-lamp",
        "/Users/feifanluo/Projects/win-res/UnifiedLdvRN/list.txt"
    )

    fun onChangeProjectPath(path: String) {
        setState {
            copy(projectPath = path)
        }
    }

    fun onChangeIdsTextPath(path: String) {
        setState {
            copy(idsTextPath = path)
        }
    }

    fun onConsoleOutputs(output: String) {
        setState {
            copy(consoleOutputs = consoleOutputs.toMutableList().apply {
                add(output)
            })
        }
    }

    fun onConsoleOutputs(outputs: List<String>) {
        setState {
            copy(consoleOutputs = consoleOutputs.toMutableList().apply {
                addAll(outputs)
            })
        }
    }

    suspend fun readMiniIds(): List<String> {
        val content = withContext(Dispatchers.IO) { readFile(state.idsTextPath) }
        val miniIds = content.split("\n").map { it.trimIndent() }
        setState {
            copy(miniIds = miniIds)
        }
        return miniIds
    }

    fun onConsoleIsRefreshLast(output: String): Boolean {
        return state.consoleOutputs.last()
            .contains("读取业务工程文件") && output.contains("读取业务工程文件")
                || state.consoleOutputs.last()
            .contains("压缩业务工程文件") && output.contains("压缩业务工程文件")
                || state.consoleOutputs.last().contains("上传中") && output.contains("上传中")
    }

    fun onConsoleRefreshLast(output: String) {
        setState {
            copy(consoleOutputs = consoleOutputs.toMutableList().apply {
                this[lastIndex] = output
            })
        }
    }

    private inline fun setState(update: MainState.() -> MainState) {
        state = state.update()
    }

    data class MainState(
        val projectPath: String,
        val idsTextPath: String,
        val miniIds: List<String> = emptyList(),
        val consoleOutputs: List<String> = emptyList()
    )
}