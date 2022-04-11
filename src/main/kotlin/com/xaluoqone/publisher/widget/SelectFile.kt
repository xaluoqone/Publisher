package com.xaluoqone.publisher.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xaluoqone.publisher.utils.openFileDialog
import kotlinx.coroutines.launch

@Composable
fun SelectFile(
    isFolder: Boolean,
    path: String,
    contentDesc: String = "选择${if (isFolder) "文件夹" else "文件"}",
    onPathChange: (String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(Modifier.padding(top = 5.dp).height(30.dp)) {
        BasicTextField(
            path,
            singleLine = true,
            onValueChange = onPathChange,
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFCCCCCC))
                .padding(horizontal = 10.dp)
                .wrapContentHeight(),
            textStyle = TextStyle(fontSize = 12.sp)
        )
        IconButton({
            scope.launch {
                onPathChange(openFileDialog(isFolder))
            }
        }, Modifier.size(30.dp).align(Alignment.CenterEnd)) {
            Icon(
                painterResource(if (isFolder) "folder.svg" else "file.svg"),
                contentDesc,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}