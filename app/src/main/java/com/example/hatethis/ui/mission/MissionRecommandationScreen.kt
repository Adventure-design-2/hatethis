package com.example.hatethis.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hatethis.model.Mission

@Composable
fun MissionRecommendationScreen(
    recommendedMission: Mission?,
    onMissionCompleted: () -> Unit
) {
    if (recommendedMission == null) {
        Text(
            text = "추천할 미션이 없습니다!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = recommendedMission.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = recommendedMission.detail)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onMissionCompleted) {
                    Text(text = "미션 완료")
                }
            }
        }
    }
}
