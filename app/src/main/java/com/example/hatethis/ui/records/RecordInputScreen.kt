package com.example.hatethis.ui.records

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.viewmodel.RecordViewModel
import kotlinx.coroutines.launch

@Composable
fun RecordInputScreen(
    viewModel: RecordViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val imageUri: Uri? = null // 이미지 URI 저장
    val coroutineScope = rememberCoroutineScope() // 코루틴 스코프 생성

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "기록 작성", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { /* 이미지 선택 로직 */ }) {
            Text("이미지 업로드")
        }

        Button(
            onClick = {
                coroutineScope.launch { // 코루틴에서 suspend 함수 호출
                    viewModel.saveRecordWithImage(
                        title = title,
                        content = content,
                        imageUri = imageUri
                    ) { success ->
                        if (success) {
                            println("기록 저장 성공!")
                        } else {
                            println("기록 저장 실패!")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }
}

@Preview
@Composable
fun PreviewRecordInputScreen() {
    RecordInputScreen()
}
