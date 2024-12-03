package com.example.hatethis.ui.records

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.hatethis.viewmodel.RecordViewModel
import kotlinx.coroutines.launch

@Composable
fun RecordScreen(viewModel: RecordViewModel) {
    val state by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 사진 업로드 섹션
        PhotoUpload(
            photoUrl = state.photoUrl,
            onPhotoSelected = { uri -> viewModel.onPhotoSelected(uri) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 텍스트 입력 섹션
        TextInput(
            text = state.text,
            onTextChange = { newText -> viewModel.onTextChanged(newText) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 감정 선택 섹션
        EmotionSelector(
            selectedEmotion = state.emotion,
            onEmotionSelected = { emotion -> viewModel.onEmotionSelected(emotion) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 저장 버튼
        SaveButton(
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveRecord()
                }
            }
        )

        // 저장 상태 표시
        if (state.isSaved) {
            Text(
                text = "Record Saved!",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PhotoUpload(photoUrl: String?, onPhotoSelected: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPhotoSelected(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Gray)
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUrl),
                contentDescription = "Selected photo",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("Tap to upload a photo", color = Color.White)
        }
    }
}

@Composable
fun TextInput(text: String, onTextChange: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Enter your text") }
    )
}

@Composable
fun EmotionSelector(selectedEmotion: String?, onEmotionSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Happy", "Sad", "Neutral").forEach { emotion ->
            val isSelected = selectedEmotion == emotion
            Text(
                text = emotion,
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onEmotionSelected(emotion) }
                    .padding(16.dp),
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun SaveButton(onSaveClick: () -> Unit) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save")
    }
}

