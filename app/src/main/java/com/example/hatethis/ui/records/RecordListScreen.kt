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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.viewmodel.RecordViewModel

@Preview
@Composable
fun RecordListScreen(
    viewModel: RecordViewModel = viewModel()
) {
    val records = viewModel.records.collectAsState(initial = emptyList())

    // Firestore에서 기록 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "기록 목록", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records.value) { record ->
                RecordItem(record)
            }
        }
    }
}

@Composable
fun RecordItem(record: com.example.hatethis.model.SharedRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${record.title}")
            Text(text = "내용: ${record.content}")
            if (record.imageUrl != null) {
                Text(text = "이미지 URL: ${record.imageUrl}")
            }
        }
    }
}
