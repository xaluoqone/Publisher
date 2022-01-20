package com.xaluoqone.publisher.utils

import com.sun.javafx.application.PlatformImpl
import javafx.application.Platform
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage

fun openFolderChooser(callback: (String) -> Unit) {
    PlatformImpl.startup {
        Platform.runLater {
            DirectoryChooser().apply {
                title = "选择文件夹"
                val file = showDialog(Stage())
                if (file != null) {
                    callback(file.absolutePath)
                }
            }
        }
    }
}

fun openFileChooser(callback: (String) -> Unit) {
    PlatformImpl.startup {
        Platform.runLater {
            FileChooser().apply {
                title = "选择文件"
                val file = showOpenDialog(Stage())
                if (file != null) {
                    callback(file.absolutePath)
                }
            }
        }
    }
}