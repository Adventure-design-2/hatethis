package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.model.Mission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MissionRecommendationViewModel(private val repository: DataRepository) : ViewModel() {

    private val _recommendedMissions = MutableStateFlow<List<Pair<Mission, Double>>>(emptyList())
    val recommendedMissions: StateFlow<List<Pair<Mission, Double>>> = _recommendedMissions

    fun recommendMissions() {
        viewModelScope.launch {
            try {
                val allMissions = repository.getLocalMissions()

                // 추천 점수 계산 및 정렬
                val recommended = allMissions
                    .map { mission ->
                        mission to calculateScore(mission)
                    }
                    .sortedByDescending { it.second } // 점수 높은 순으로 정렬

                _recommendedMissions.value = recommended
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateScore(mission: Mission): Double {
        // 추천 점수 = (environment + 1) / (1 + completedCount)
        return (mission.environment + 1).toDouble() / (1 + mission.completedCount)
    }

    fun markMissionCompleted(mission: Mission) {
        viewModelScope.launch {
            try {
                // 완료 횟수 증가
                mission.completedCount++

                // 로컬 데이터 업데이트
                repository.updateMission(mission)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
