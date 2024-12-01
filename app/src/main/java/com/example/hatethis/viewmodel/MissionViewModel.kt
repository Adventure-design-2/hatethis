package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.model.Mission
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MissionViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // 미션 목록 상태 관리
    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions

    // Firestore에서 미션 목록 불러오기
    fun loadMissions() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("missions")
                    .get()
                    .await()

                val missionList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Mission::class.java)
                }
                _missions.value = missionList
            } catch (e: Exception) {
                e.printStackTrace()
                _missions.value = emptyList() // 실패 시 빈 목록 반환
            }
        }
    }
}
