package com.xaluoqone.publisher

import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.javafx.application.PlatformImpl
import javafx.application.Platform
import javafx.stage.DirectoryChooser
import javafx.stage.Stage

@Composable
@Preview
fun App() {
    var projectPath by remember { mutableStateOf("") }

    MaterialTheme {
        Column(Modifier.padding(12.dp)) {
            Text("当前选择的小程序项目：$projectPath")
            Row(Modifier.padding(top = 10.dp).height(35.dp)) {
                Box(
                    Modifier.fillMaxHeight()
                        .weight(1f)
                        .padding(end = 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.LightGray),
                    Alignment.CenterStart
                ) {
                    BasicTextField(projectPath, onValueChange = {
                        projectPath = it
                    }, modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth())
                }
                Button(onClick = {
                    openFolderChooser {
                        projectPath = it
                    }
                }) {
                    Text("选择文件夹")
                }
            }
        }
    }
}

fun openFolderChooser(callback: (String) -> Unit) {
    PlatformImpl.startup {
        Platform.runLater {
            DirectoryChooser().apply {
                title = "选择文件夹"
                val file = showDialog(Stage())
                if (file != null) {
                    callback(file.absolutePath)
                    println("选择文件夹:${file.absolutePath}")
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Publisher") {
        App()
    }
}
