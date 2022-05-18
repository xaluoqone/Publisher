package com.xaluoqone.publisher

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.xaluoqone.publisher.ui.widget.SelectFile
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.xaluoqone.publisher.store.MainStore
import com.xaluoqone.publisher.ui.theme.AppTheme
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
    var database by remember { mutableStateOf<Database?>(null) }

    LaunchedEffect(Unit) {
        database = withContext(Dispatchers.IO) {
            val driver =
                //JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                JdbcSqliteDriver("jdbc:sqlite:${System.getProperty("compose.application.resources.dir")}/database.db")
            Database.Schema.create(driver)
            val db = Database(driver)
            val config = db.configQueries.selectConfig().executeAsOneOrNull()
            store.onConsoleOutputs("读取数据库配置：${config}")
            if (config == null) {
                db.configQueries.insert("", "")
            } else {
                store.onChangeIdsTextPath(config.idsTextPath)
                store.onChangeProjectPath(config.projectPath)
            }
            db
        }
    }

    LaunchedEffect(state.idsTextPath) {
        if (state.idsTextPath.isEmpty()) {
            return@LaunchedEffect
        }
        if (!state.idsTextPath.toPath().toFile().exists()) {
            store.onConsoleOutputs("${state.idsTextPath}不存在！")
            return@LaunchedEffect
        }
        store.onConsoleOutputs("正在解析文件：${state.idsTextPath}")
        store.onConsoleOutputs(store.readMiniIds())
        store.onConsoleOutputs("${state.idsTextPath} 解析完成")
    }

    LaunchedEffect(state.consoleOutputs) {
        if (state.consoleOutputs.isNotEmpty() && !listState.isScrollInProgress) {
            listState.animateScrollToItem(state.consoleOutputs.lastIndex)
        }
    }

    Column(Modifier.padding(10.dp)) {
        Text("当前选择的小程序项目：${state.projectPath}", fontSize = 12.sp)
        SelectFile(true, state.projectPath) {
            if (it != null) {
                store.onChangeProjectPath(it)
                database?.configQueries?.updateProjectPath(it)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("配置小程序ID文本文档路径：${state.idsTextPath}", fontSize = 12.sp)
        SelectFile(false, state.idsTextPath) {
            if (it != null) {
                store.onChangeIdsTextPath(it)
                database?.configQueries?.updateIdsTextPath(it)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("控制台：", fontSize = 12.sp)
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)).background(AppTheme.colors.background)
        ) {
            LazyColumn(
                Modifier.fillMaxSize(), listState, contentPadding = PaddingValues(5.dp)
            ) {
                items(state.consoleOutputs) {
                    SelectionContainer { Text(it, fontSize = 12.sp) }
                }
            }
            Column(Modifier.fillMaxHeight().align(Alignment.CenterEnd), verticalArrangement = Arrangement.Bottom) {
                IconButton({}) {
                    Icon(painterResource("switch.svg"), "切换服务器", tint = AppTheme.colors.primary)
                }
                IconButton({
                    coroutineScope.launch {
                        store.onConsoleOutputs("开始登录EZM...")
                        withContext(Dispatchers.IO) {
                            execCmd(cmd = arrayOf("ezm", "login", "--un=b.li2@ledvance.com", "--pw=test12345+"),
                                execPath = state.projectPath,
                                onRead = {
                                    store.onConsoleOutputs(it)
                                })
                        }
                    }
                }) {
                    Icon(painterResource("login.svg"), "登录 EZM", tint = AppTheme.colors.primary)
                }
                IconButton({
                    if (state.projectPath.isEmpty()) {
                        store.onConsoleOutputs("小程序目录不能为空！")
                        return@IconButton
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
                            val packageName = indexContent.substring(pathIndex + "./src/".length, pathIndexEnd - 1)
                            val newIndexContent = indexContent.replace(packageName, miniId)
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
                                    return@publish
                                }
                                store.onConsoleOutputs("文件夹名修改完成！")
                                store.onConsoleOutputs("开始 publish ↑")
                                withContext(Dispatchers.IO) {
                                    execCmd(
                                        cmd = arrayOf("ezm", "publish"), execPath = state.projectPath
                                    ) { result ->
                                        if (result.isNotBlank() && !result.contains("已拷贝")) {
                                            if (store.onConsoleIsRefreshLast(result)) {
                                                store.onConsoleRefreshLast(result)
                                            } else {
                                                store.onConsoleOutputs(result)
                                            }
                                        }
                                    }
                                }
                            } else {
                                store.onConsoleOutputs("src目录为空！批量上传中止！")
                                return@publish
                            }
                            store.onConsoleOutputs("√ 上传完成，${miniId} publish结束")
                            delay(1000)
                        }
                        store.onConsoleOutputs("√ 批量上传完成")
                    }
                }) {
                    Icon(painterResource("publish.svg"), "批量发布小程序包", tint = AppTheme.colors.primary)
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, title = "萤石小程序发布工具", icon = painterResource("icon.ico")
    ) {
        AppTheme(AppTheme.Theme.Teal) {
            App()
        }
    }
}