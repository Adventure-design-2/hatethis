package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.LocalDataStore
import com.example.hatethis.data.toEntity
import com.example.hatethis.model.Mission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MissionRecommendationViewModel(
    private val repository: LocalDataStore
) : ViewModel() {

    private val _missions = MutableStateFlow<List<Mission>>(emptyList()) // 전체 미션 목록
    val missions: StateFlow<List<Mission>> = _missions

    private val _recommendedMissions = MutableStateFlow<List<Pair<Mission, Int>>>(emptyList())
    val recommendedMissions: StateFlow<List<Pair<Mission, Int>>> = _recommendedMissions

    // 로컬 저장소에서 미션 로드 및 추천 알고리즘 적용
    fun loadAndRecommendMissions() {
        viewModelScope.launch {
            try {
                // 로컬 데이터베이스에서 모든 미션 가져오기
                val allMissions = repository.getAllMissions().map { it.toDomain() }

                // 추천 알고리즘 적용
                val recommended = allMissions.map { mission ->
                    mission to calculateScore(mission).toInt()
                }.sortedByDescending { it.second } // 점수 내림차순 정렬

                // UI 상태 업데이트
                _missions.emit(allMissions)
                _recommendedMissions.emit(recommended)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 추천 점수 계산
    private fun calculateScore(mission: Mission): Double {
        // 추천 점수 = (environment + 1) / (1 + completedCount)
        return (mission.environment + 1).toDouble() / (1 + mission.completedCount)
    }

    // 미션 완료 상태 업데이트
    fun markMissionCompleted(mission: Mission) {
        viewModelScope.launch {
            try {
                // 완료 횟수 증가
                mission.completedCount++

                // 로컬 데이터 업데이트
                repository.updateMission(mission.toEntity())

                // 추천 목록 갱신
                loadAndRecommendMissions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 로컬 데이터에서 미션 로드
    fun loadLocalMissions() {
        viewModelScope.launch {
            try {
                val missions = repository.getAllMissions().map { it.toDomain() }
                _missions.emit(missions)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 초기 기본 미션 설정
    fun initializeDefaultMissions(defaultMissions: List<Mission>) {
        viewModelScope.launch {
            try {
                val existingMissions = repository.getAllMissions()
                if (existingMissions.isEmpty()) {
                    repository.saveMissions(defaultMissions.map { it.toEntity() })
                }
                loadAndRecommendMissions() // 기본 미션 설정 후 추천 목록 갱신
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
