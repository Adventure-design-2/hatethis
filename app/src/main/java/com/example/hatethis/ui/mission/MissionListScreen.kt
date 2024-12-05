package com.example.hatethis.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hatethis.model.Mission

@Composable
fun MissionListScreen(
    missions: List<Mission>,
    onMissionSelected: (Mission) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(missions) { mission ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { onMissionSelected(mission) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = mission.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "완료 횟수: ${mission.completedCount}")
                    Text(text = "환경 점수: ${mission.environment}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissionListScreenPreview() {
    val sampleMissions = listOf(
        Mission(
            title = "공원에서 산책하기",
            environment = 8,
            locationTag = listOf("공원", "자연"),
            detail = "친환경적인 환경에서 여유로운 산책을 즐겨보세요.",
            completedCount = 2,
            action = "walk_action"
        ),
        Mission(
            title = "도서관 방문",
            environment = 5,
            locationTag = listOf("도서관", "공부"),
            detail = "조용한 도서관에서 독서를 즐기며 마음의 양식을 쌓아보세요.",
            completedCount = 0,
            action = "library_action"
        )
    )
    MissionListScreen(missions = sampleMissions, onMissionSelected = {})
}