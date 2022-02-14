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
import com.xaluoqone.publisher.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

@Composable
@Preview
fun App() {
    var projectPath by remember { mutableStateOf("""C:\xaluoqone\UnifiedLdvRN\smart-unified-home-RN\ldv-iotapp-base-lamp""") }
    var idsTextPath by remember { mutableStateOf("""C:\xaluoqone\UnifiedLdvRN\list-test.txt""") }
    val cmdRes = remember { mutableStateListOf<String>() }
    val miniIds = remember { mutableListOf<String>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(idsTextPath) {
        withContext(Dispatchers.Main) {
            cmdRes.add("正在解析文件：$idsTextPath")
            val content = withContext(Dispatchers.IO) { readFile(idsTextPath) }
            miniIds.addAll(content.split("\n").map { it.trimIndent() })
            cmdRes.add(content)
            cmdRes.add("$idsTextPath 解析完成")
            listState.animateScrollToItem(cmdRes.lastIndex)
        }
    }

    MaterialTheme {
        Column(Modifier.padding(10.dp)) {
            Text("当前选择的小程序项目：$projectPath", fontSize = 12.sp)
            SelectFile(true, projectPath) {
                projectPath = it
            }
            Spacer(Modifier.height(10.dp))
            Text("配置小程序ID文本文档路径：$idsTextPath", fontSize = 12.sp)
            SelectFile(false, idsTextPath) { idsTextPath = it }
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
                            SelectionContainer { Text(it, fontSize = 12.sp) }
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
                                cmdRes.add("开始登录EZM...")
                                withContext(Dispatchers.IO) {
                                    execCmd(
                                        cmd = arrayOf("ezm", "login", "--un=b.li2@ledvance.com", "--pw=test12345+"),
                                        execPath = projectPath,
                                        onRead = {
                                            cmdRes.add(it)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(cmdRes.lastIndex)
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
                            if (projectPath.isEmpty()) {
                                cmdRes.add("小程序目录不能为空！")
                                coroutineScope.launch {
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                }
                                return@Button
                            }
                            coroutineScope.launch publish@{
                                miniIds.forEach { miniId ->
                                    val projectPathFix =
                                        if (projectPath.last() == '\\') projectPath else """$projectPath\"""
                                    val indexFile = "${projectPathFix}index.js"
                                    val indexContent = withContext(Dispatchers.IO) { readFile(indexFile) }
                                    cmdRes.add("开始修改${indexFile}")
                                    val pathIndex = indexContent.indexOf("./src/")
                                    val pathIndexEnd = indexContent.indexOf(");")
                                    val packageName =
                                        indexContent.substring(pathIndex + "./src/".length, pathIndexEnd - 1)
                                    val newIndexContent =
                                        indexContent.replace(packageName, miniId)
                                    withContext(Dispatchers.IO) { writeFile(indexFile, newIndexContent) }
                                    cmdRes.add("修改${indexFile}完成！")
                                    val srcPath = """${projectPathFix}src\${packageName}"""
                                    cmdRes.add("开始修改文件夹名：${packageName}")
                                    val targetName = """${projectPathFix}src\${miniId}""".toPath().toFile()
                                    val renameRes = srcPath.toPath().toFile().renameTo(targetName)
                                    if (!renameRes) {
                                        cmdRes.add("文件夹名修改失败！批量上传中止！")
                                        listState.animateScrollToItem(cmdRes.lastIndex)
                                        return@publish
                                    }
                                    cmdRes.add("文件夹名已修改：${targetName.absolutePath}")
                                    cmdRes.add("开始 publish ===================================>")
                                    listState.animateScrollToItem(cmdRes.lastIndex)
                                    withContext(Dispatchers.IO) {
                                        execCmd(
                                            cmd = arrayOf("ezm", "publish"),
                                            execPath = projectPath
                                        ) { result ->
                                            if (result.isNotBlank() && !result.contains("已拷贝")) {
                                                if (
                                                    cmdRes.last().contains("读取业务工程文件") && result.contains("读取业务工程文件")
                                                    || cmdRes.last().contains("压缩业务工程文件") && result.contains("压缩业务工程文件")
                                                    || cmdRes.last().contains("上传中") && result.contains("上传中")
                                                ) {
                                                    cmdRes[cmdRes.lastIndex] = result
                                                } else {
                                                    cmdRes.add(result)
                                                    if (!listState.isScrollInProgress) {
                                                        coroutineScope.launch {
                                                            listState.animateScrollToItem(cmdRes.lastIndex)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    cmdRes.add("√ 上传完成，${miniId} publish结束")
                                }
                                cmdRes.add("√ 批量上传完成")
                                listState.animateScrollToItem(cmdRes.lastIndex)
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
