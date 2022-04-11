package com.xaluoqone.publisher.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.FileDialog

@Composable
fun SelectFile(isFolder: Boolean, path: String, onPathChange: (String) -> Unit) {
    Row(Modifier.padding(top = 5.dp).height(30.dp)) {
        Box(
            Modifier.clip(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFCCCCCC)),
            Alignment.CenterStart
        ) {
            BasicTextField(
                path,
                onValueChange = onPathChange,
                modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
                textStyle = TextStyle(fontSize = 12.sp)
            )
        }
        Button(
            onClick = {
                System.setProperty("apple.awt.fileDialogForDirectories", isFolder.toString())
                val dialog = FileDialog(ComposeWindow())
                dialog.isVisible = true
                println(if (isFolder) dialog.directory else "${dialog.directory}${dialog.file}")
                if (dialog.directory != null) {
                    onPathChange(if (isFolder) dialog.directory else "${dialog.directory}${dialog.file}")
                }
            },
            Modifier.fillMaxHeight().defaultMinSize(100.dp),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Text(if (isFolder) "选择文件夹" else "选择文件", fontSize = 12.sp)
        }
    }
}