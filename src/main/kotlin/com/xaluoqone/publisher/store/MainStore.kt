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
        """C:\xaluoqone\UnifiedLdvRN\smart-unified-home-RN\ldv-iotapp-base-lamp""",
        """C:\xaluoqone\UnifiedLdvRN\list-test.txt"""
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

    suspend fun readMiniIds():String {
        val content = withContext(Dispatchers.IO) { readFile(state.idsTextPath) }
        setState {
            copy(miniIds = content.split("\n").map { it.trimIndent() })
        }
        return content
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