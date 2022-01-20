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
import androidx.compose.ui.Alignment
import com.xaluoqone.publisher.utils.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

@Composable
@Preview
fun App() {
    var projectPath by remember { mutableStateOf("") }
    var ezmPath by remember { mutableStateOf("C:/Users/X.Nong-ext/AppData/Roaming/npm/ezm") }
    val cmdRes = remember { mutableStateListOf<String>() }
    val miniIds = remember { mutableListOf<String>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column(Modifier.padding(10.dp)) {
            Text("当前选择的小程序项目：$projectPath", fontSize = 12.sp)
            SelectFile(projectPath) {
                projectPath = it
            }
            Spacer(Modifier.height(10.dp))
            Text("ezm 路径：$ezmPath", fontSize = 12.sp)
            SelectFile(ezmPath) {
                ezmPath = it
            }
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
                        items(cmdRes) {
                            Text(it, fontSize = 12.sp)
                        }
                    }
                    if (cmdRes.isNotEmpty()) {
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
                                execCmd(
                                    cmd = "$ezmPath login --un=b.li2@ledvance.com --pw=test12345+",
                                    execPath = projectPath,
                                    finishFlag = "√ 登录完成",
                                ) {
                                    cmdRes.add(it)
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                }
                                cancel()
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
                            openFileChooser { filePath ->
                                cmdRes.add("正在解析文件：$filePath")
                                coroutineScope.launch {
                                    val content = readFile(filePath)
                                    miniIds.addAll(content.split("\n").map {
                                        it.trimIndent()
                                    })
                                    cmdRes.add(content)
                                    cmdRes.add("$filePath 解析完成")
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                }
                            }
                        },
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.height(36.dp).fillMaxWidth()
                    ) {
                        Text("配置文件", fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (projectPath.isEmpty()) {
                                cmdRes.add("小程序目录不能为空！")
                                coroutineScope.launch {
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                }
                                return@Button
                            }
                            coroutineScope.launch {
                                miniIds.forEach { miniId ->
                                    val projectPathFix =
                                        if (projectPath.last() == '\\') projectPath else """$projectPath\"""
                                    val indexFile = "${projectPathFix}index.js"
                                    val indexContent = readFile(indexFile)
                                    cmdRes.add("获取${indexFile}内容：")
                                    cmdRes.add(indexContent)
                                    cmdRes.add("开始修改${indexFile}")
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                    val pathIndex = indexContent.indexOf("./src/")
                                    val pathIndexEnd = indexContent.indexOf(");")
                                    val packageName =
                                        indexContent.substring(pathIndex + "./src/".length, pathIndexEnd - 1)
                                    val newIndexContent =
                                        indexContent.replace(packageName, miniId)
                                    writeFile(indexFile, newIndexContent)
                                    cmdRes.add("修改${indexFile}完成！")
                                    cmdRes.add(newIndexContent)
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                    val srcPath = """${projectPathFix}src\${packageName}"""
                                    cmdRes.add("开始修改文件夹名：${packageName}")
                                    val targetName = """${projectPathFix}src\${miniId}""".toPath().toFile()
                                    val renameRes = srcPath.toPath().toFile().renameTo(targetName)
                                    if (renameRes) {
                                        cmdRes.add("文件夹名已修改：${targetName.absolutePath}")
                                        cmdRes.add("开始 publish ===================================>")
                                        /*listState.animateScrollToItem(cmdRes.lastIndex)
                                        execCmd(
                                            cmd = "$ezmPath publish",
                                            execPath = projectPath,
                                            finishFlag = "√ 上传完成，publish结束",
                                        ) {
                                            *//*cmdRes.add(it)
                                        listState.animateScrollToItem(cmdRes.lastIndex)*//*
                                        }
                                        cmdRes.add("√ 上传完成，${miniId} publish结束")*/
                                    }
                                }
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
        Window(onCloseRequest = ::exitApplication, title = "萤石小程序发布工具") {
            App()
        }
    }
}
