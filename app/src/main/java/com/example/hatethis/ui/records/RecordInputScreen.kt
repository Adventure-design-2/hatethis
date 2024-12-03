package com.example.hatethis.ui.records

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.hatethis.viewmodel.RecordViewModel
import kotlinx.coroutines.launch

@Composable
fun RecordInputScreen(
    viewModel: RecordViewModel = viewModel(),
    userUid: String,
    onNavigateToRecordList: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "기록 작성", style = MaterialTheme.typography.titleLarge)

        // 이미지 업로드 버튼
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이미지 업로드")
        }

        // 업로드된 이미지 미리보기
        if (imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Uploaded Image",
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "이미지가 없습니다", color = Color.Gray)
            }
        }

        // 제목 입력 필드
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth()
        )

        // 내용 입력 필드
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth()
        )

        // 저장 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.saveRecordWithImage(
                        title = title,
                        content = content,
                        imageUri = imageUri,
                        userUid = userUid
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "기록이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                            onNavigateToRecordList()
                        } else {
                            Toast.makeText(context, "기록 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
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
