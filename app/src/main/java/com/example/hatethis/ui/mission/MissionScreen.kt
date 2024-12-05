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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hatethis.model.Mission
import com.example.hatethis.viewmodel.MissionRecommendationViewModel

@Composable
fun MissionScreen(
    viewModel: MissionRecommendationViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToRecordList: () -> Unit,
    onNavigateToRecordInput: () -> Unit,
    onNavigateToAllMissions: () -> Unit // 전체 미션 화면으로 이동하는 콜백
) {
    // 추천된 미션 상태 관찰
    val missionsWithScores by viewModel.recommendedMissions.collectAsState(initial = emptyList())

    // 미션 로드 및 추천
    LaunchedEffect(Unit) {
        viewModel.loadAndRecommendMissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateToRecordList) {
                Text("기록 목록 보기")
            }

            Button(onClick = onNavigateToProfile) {
                Text("프로필로 이동")
            }

            Button(onClick = onNavigateToAllMissions) { // 전체 미션 화면 버튼
                Text("전체 미션 보기")
            }
        }

        // 기록 작성 버튼
        Button(
            onClick = onNavigateToRecordInput,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("기록 작성하기")
        }

        // 미션 목록 표시
        if (missionsWithScores.isEmpty()) {
            Text("추천된 미션이 없습니다.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(missionsWithScores) { (mission, score) ->
                    MissionItem(mission = mission, score = score)
                }
            }
        }
    }
}

@Composable
fun MissionItem(mission: Mission, score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${mission.title}")
            Text(text = "환경 점수: ${mission.environment}")
            Text(text = "추천 점수: $score")
            Text(text = "태그: ${mission.locationTag.joinToString(", ")}")
            Text(text = "설명: ${mission.detail}")
        }
    }
}
