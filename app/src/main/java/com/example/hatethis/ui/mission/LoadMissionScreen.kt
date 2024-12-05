package com.example.hatethis.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.model.Mission
import com.example.hatethis.viewmodel.MissionRecommendationViewModel

@Composable
fun LocalMissionScreen(
    viewModel: MissionRecommendationViewModel,
    onNavigateBack: () -> Unit
) {
    val localMissions by viewModel.recommendedMissions.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadLocalMissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onNavigateBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("뒤로가기")
        }

        if (localMissions.isEmpty()) {
            Text("저장된 미션이 없습니다.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(localMissions) { (mission, score) ->
                    LocalMissionItem(mission = mission, score = score.toInt())
                }
            }
        }
    }
}

@Composable
fun LocalMissionItem(mission: Mission, score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "제목: ${mission.title}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "환경 점수: ${mission.environment}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "추천 점수: $score", style = MaterialTheme.typography.bodySmall)
        }
    }
}
