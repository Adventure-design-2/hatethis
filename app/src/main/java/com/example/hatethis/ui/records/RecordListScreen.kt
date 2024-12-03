package com.example.hatethis.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.model.SharedRecord
import com.example.hatethis.viewmodel.RecordViewModel



@Composable
fun RecordListScreen(
    viewModel: RecordViewModel = viewModel(),
    userUid: String // 사용자 UID 전달
) {
    val records by viewModel.records.collectAsState(initial = emptyList())

    // Firestore에서 기록 불러오기
    LaunchedEffect(userUid) {
        println("LaunchedEffect: Fetching records for $userUid")
        viewModel.fetchSharedRecords(userUid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "기록 목록", style = MaterialTheme.typography.titleLarge)

        if (records.isEmpty()) {
            Text(
                text = "기록이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    RecordItem(record)
                }
            }
        }
    }
}



@Composable
fun RecordItem(record: SharedRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${record.title}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "내용: ${record.content}", style = MaterialTheme.typography.bodyMedium)
            if (record.imageUrl != null) {
                Text(
                    text = "이미지 URL: ${record.imageUrl}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "작성 시간: ${record.createdAt.toDate()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewRecordListScreen() {
    val dummyRecords = listOf(
        SharedRecord(
            id = "1",
            title = "테스트 제목 1",
            content = "테스트 내용 1",
            isShared = true,
            imageUrl = "https://example.com/image1.jpg",
            authorIds = listOf("user1", "partner1"), // authorIds로 수정
            createdAt = com.google.firebase.Timestamp.now()
        ),
        SharedRecord(
            id = "2",
            title = "테스트 제목 2",
            content = "테스트 내용 2",
            isShared = true,
            imageUrl = "https://example.com/image2.jpg",
            authorIds = listOf("user1", "partner1"), // authorIds로 수정
            createdAt = com.google.firebase.Timestamp.now()
        )
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "기록 목록", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyRecords) { record ->
                RecordItem(record)
            }
        }
    }
}

