package com.example.hatethis.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hatethis.model.Mission
import com.example.hatethis.viewmodel.MissionRecommendationViewModel

@Composable
fun MissionScreen(
    viewModel: MissionRecommendationViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToRecordList: () -> Unit,
    onNavigateToRecordInput: () -> Unit
) {
    val missions = viewModel.recommendedMissions.collectAsState()

    // Firestore에서 미션 로드
    LaunchedEffect(Unit) {
        viewModel.recommendMissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateToRecordList) {
                Text(text = "기록 목록 보기")
            }

            Button(onClick = onNavigateToProfile) {
                Text(text = "프로필로 이동")
            }
        }

        Button(
            onClick = onNavigateToRecordInput,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = "기록 작성하기")
        }

        Text(
            text = "추천 미션 목록",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(missions.value) { mission ->
                MissionItem(
                    mission = mission.first,
                    score = mission.second
                )
            }
        }
    }
}

@Composable
fun MissionItem(mission: Mission, score: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${mission.title}")
            Text(text = "환경: ${mission.environment}")
            Text(text = "태그: ${mission.locationTag.joinToString(", ")}")
            Text(text = "설명: ${mission.detail}")
            Text(text = "추천 점수: ${"%.2f".format(score)}")
        }
    }
}
