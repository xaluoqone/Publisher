package com.xaluoqone.publisher

import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.xaluoqone.publisher.widget.SelectFile
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.xaluoqone.publisher.store.MainStore
import com.xaluoqone.publisher.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

@Composable
@Preview
fun App() {
    val store = remember { MainStore() }
    val state = store.state
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.idsTextPath) {
        withContext(Dispatchers.Main) {
            if (!state.idsTextPath.toPath().toFile().exists()) {
                store.onConsoleOutputs("${state.idsTextPath}不存在！")
                return@withContext
            }
            store.onConsoleOutputs("正在解析文件：${state.idsTextPath}")
            store.onConsoleOutputs(store.readMiniIds())
            store.onConsoleOutputs("${state.idsTextPath} 解析完成")
            listState.animateScrollToItem(state.consoleOutputs.lastIndex)
        }
    }

    MaterialTheme {
        Column(Modifier.padding(10.dp)) {
            Text("当前选择的小程序项目：${state.projectPath}", fontSize = 12.sp)
            SelectFile(true, state.projectPath) {
                store.onChangeProjectPath(it)
            }
            Spacer(Modifier.height(10.dp))
            Text("配置小程序ID文本文档路径：${state.idsTextPath}", fontSize = 12.sp)
            SelectFile(false, state.idsTextPath) { store.onChangeIdsTextPath(it) }
            Spacer(Modifier.height(10.dp))
            Text("控制台：", fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Row {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFCCCCCC))
                            .padding(5.dp),
                        listState,
                    ) {
                        items(state.consoleOutputs) {
                            SelectionContainer { Text(it, fontSize = 12.sp) }
                        }
                    }
                    if (state.consoleOutputs.isNotEmpty()) {
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(bottom = 5.dp),
                            adapter = rememberScrollbarAdapter(
                                scrollState = listState
                            )
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.width(100.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                store.onConsoleOutputs("开始登录EZM...")
                                withContext(Dispatchers.IO) {
                                    execCmd(
                                        cmd = arrayOf("ezm", "login", "--un=b.li2@ledvance.com", "--pw=test12345+"),
                                        execPath = state.projectPath,
                                        onRead = {
                                            store.onConsoleOutputs(it)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.height(36.dp).fillMaxWidth()
                    ) {
                        Text("登录ezm", fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (state.projectPath.isEmpty()) {
                                store.onConsoleOutputs("小程序目录不能为空！")
                                coroutineScope.launch {
                                    listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                }
                                return@Button
                            }
                            coroutineScope.launch publish@{
                                state.miniIds.forEach { miniId ->
                                    val projectPathFix =
                                        if (state.projectPath.last() == '\\') state.projectPath else """${state.projectPath}\"""
                                    val indexFile = "${projectPathFix}index.js"
                                    val indexContent = withContext(Dispatchers.IO) { readFile(indexFile) }
                                    store.onConsoleOutputs("开始修改${indexFile}")
                                    val pathIndex = indexContent.indexOf("./src/")
                                    val pathIndexEnd = indexContent.indexOf(");")
                                    val packageName =
                                        indexContent.substring(pathIndex + "./src/".length, pathIndexEnd - 1)
                                    val newIndexContent =
                                        indexContent.replace(packageName, miniId)
                                    withContext(Dispatchers.IO) { writeFile(indexFile, newIndexContent) }
                                    store.onConsoleOutputs("修改${indexFile}完成！")
                                    val srcPath = """${projectPathFix}src""".toPath().toFile()
                                    val srcChildren = srcPath.listFiles()
                                    if (srcPath.isDirectory && !srcChildren.isNullOrEmpty()) {
                                        val miniResDir = srcChildren[0]
                                        val targetName = """${srcPath}\${miniId}""".toPath().toFile()
                                        store.onConsoleOutputs("开始修改文件夹名：${miniResDir.name}->${targetName.name}")
                                        val renameRes = miniResDir.renameTo(targetName)
                                        if (!renameRes) {
                                            store.onConsoleOutputs("文件夹名修改失败！批量上传中止！")
                                            listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                            return@publish
                                        }
                                        store.onConsoleOutputs("文件夹名修改完成！")
                                        store.onConsoleOutputs("开始 publish ↑")
                                        listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                        withContext(Dispatchers.IO) {
                                            execCmd(
                                                cmd = arrayOf("ezm", "publish"),
                                                execPath = state.projectPath
                                            ) { result ->
                                                if (result.isNotBlank() && !result.contains("已拷贝")) {
                                                    if (store.onConsoleIsRefreshLast(result)) {
                                                        store.onConsoleRefreshLast(result)
                                                    } else {
                                                        store.onConsoleOutputs(result)
                                                        if (!listState.isScrollInProgress) {
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        store.onConsoleOutputs("src目录为空！批量上传中止！")
                                        listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                                        return@publish
                                    }
                                    store.onConsoleOutputs("√ 上传完成，${miniId} publish结束")
                                    delay(1000)
                                }
                                store.onConsoleOutputs("√ 批量上传完成")
                                listState.animateScrollToItem(state.consoleOutputs.lastIndex)
                            }
                        },
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.height(36.dp).fillMaxWidth()
                    ) {
                        Text("批量发布", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "萤石小程序发布工具", icon = painterResource("icon.ico")) {
            App()
        }
    }
}
