package com.example.hatethis.ui.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hatethis.model.DateRecord
import com.example.hatethis.viewmodel.RecordListViewModel

@Composable
fun RecordListScreen(
    viewModel: RecordListViewModel,
    onNavigateToMission: () -> Unit,
    onAddNewRecord: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val records by viewModel.records.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 화면 초기화 시 기록 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "추억 저장소",
            style = MaterialTheme.typography.headlineSmall, // Material3 스타일로 변경
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 미션 화면으로 이동하는 버튼
            Button(onClick = onNavigateToMission, modifier = Modifier.weight(1f)) {
                Text(text = "미션 화면")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 기록 추가 화면으로 이동하는 버튼
            Button(onClick = onAddNewRecord, modifier = Modifier.weight(1f)) {
                Text(text = "기록 추가")
            }
        }

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "알 수 없는 오류 발생",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            records.isEmpty() -> {
                Text(
                    text = "기록이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium, // Material3 스타일로 변경
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records) { record ->
                        RecordItem(record = record, onClick = { onRecordClick(record.recordId) })
                    }
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: DateRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "미션 상태: ${record.missionStatus}",
                style = MaterialTheme.typography.bodyMedium // Material3 스타일로 변경
            )
            Text(
                text = "감정: ${record.emotion ?: "없음"}",
                style = MaterialTheme.typography.bodySmall // Material3 스타일로 변경
            )
            Text(
                text = "생성일: ${record.createdAt}",
                style = MaterialTheme.typography.bodySmall // Material3 스타일로 변경
            )
        }
    }
}
