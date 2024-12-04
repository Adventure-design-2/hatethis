package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.model.Mission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MissionViewModel(private val repository: DataRepository) : ViewModel() {

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions

    init {
        updateMissionsFromServer()
        loadLocalMissions()
    }

    // 서버에서 미션 업데이트
    private fun updateMissionsFromServer() {
        viewModelScope.launch {
            try {
                val serverMissions = repository.fetchMissionsFromServer()
                repository.updateLocalMissions(serverMissions) // 로컬 데이터 업데이트
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 로컬 저장소에서 미션 로드
    private fun loadLocalMissions() {
        viewModelScope.launch {
            val localMissions = repository.getLocalMissions()
            _missions.value = localMissions
        }
    }

    // Firestore에서 미션 로드 후 로컬 저장소 업데이트
    fun loadMissions() {
        viewModelScope.launch {
            try {
                val serverMissions = repository.fetchMissionsFromServer()
                repository.updateLocalMissions(serverMissions)
                _missions.value = repository.getLocalMissions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
