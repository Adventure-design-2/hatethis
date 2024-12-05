package com.example.hatethis.com.example.hatethis.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hatethis.data.MissionRepository
import com.example.hatethis.ui.mission.MissionListScreen
import com.example.hatethis.ui.mission.MissionRecommendationScreen

class MissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MissionRepository
        val repository = MissionRepository(applicationContext)

        setContent {
            MissionApp(repository = repository)
        }
    }
}

@Composable
fun MissionApp(repository: MissionRepository) {
    val navController = rememberNavController()
    val missions = remember { mutableStateOf(repository.loadMissions()) }

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            MissionListScreen(
                missions = missions.value,
                onMissionSelected = { mission ->
                    navController.navigate("recommend/${mission.title}")
                }
            )
        }
        composable("recommend/{missionTitle}") { backStackEntry ->
            val missionTitle = backStackEntry.arguments?.getString("missionTitle")
            val recommendedMission = missions.value.find { it.title == missionTitle }

            MissionRecommendationScreen(
                recommendedMission = recommendedMission,
                onMissionCompleted = {
                    if (recommendedMission != null) {
                        recommendedMission.completedCount++
                        repository.saveMissions(missions.value) // Save updated data
                        missions.value = repository.loadMissions() // Reload missions
                        navController.popBackStack() // Return to the list
                    }
                }
            )
        }
    }
}
