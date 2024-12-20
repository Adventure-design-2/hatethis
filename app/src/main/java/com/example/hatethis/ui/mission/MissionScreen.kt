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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.model.Mission
import com.example.hatethis.viewmodel.MissionViewModel

@Composable
fun MissionScreen(
    viewModel: MissionViewModel = viewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToRecords: () -> Unit, // 기록 목록 화면으로 이동하는 콜백 추가
    onNavigateToRecordInput: () -> Unit // 기록 작성 화면으로 이동하는 콜백 추가
) {
    val missions = viewModel.missions.collectAsState()

    // Firestore에서 미션 로드
    LaunchedEffect(Unit) {
        viewModel.loadMissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 기록 목록 화면으로 이동하는 버튼
            Button(onClick = onNavigateToRecords) {
                Text(text = "기록 목록 보기")
            }

            // 프로필 화면으로 이동하는 버튼
            Button(onClick = onNavigateToProfile) {
                Text(text = "프로필로 이동")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 기록 작성 화면으로 이동하는 버튼
        Button(
            onClick = onNavigateToRecordInput,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "기록 작성하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "미션 목록",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 미션 목록 표시
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(missions.value) { mission ->
                MissionItem(mission = mission)
            }
        }
    }
}

@Composable
fun MissionItem(mission: Mission) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${mission.missionDetails.title}")
            Text(text = "설명: ${mission.missionDetails.description}")
            Text(text = "생성 시간: ${mission.missionDetails.createdAt}")
        }
    }
}
