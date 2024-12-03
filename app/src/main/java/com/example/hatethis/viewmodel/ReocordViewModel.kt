package com.example.hatethis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.data.LocalDataStore
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.MissionStatus
import com.example.hatethis.model.RecordPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordViewModel(
    private val repository: DataRepository,
    private val localDataStore: LocalDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState

    fun onPhotoSelected(photoUri: Uri) {
        _uiState.update { it.copy(photoUrl = photoUri.toString()) }
    }

    fun onTextChanged(newText: String) {
        _uiState.update { it.copy(text = newText) }
    }

    fun onEmotionSelected(emotion: String) {
        _uiState.update { it.copy(emotion = emotion) }
    }

    fun saveRecord() {
        val currentState = _uiState.value
        if (currentState.photoUrl != null && currentState.text.isNotEmpty() && currentState.emotion != null) {
            viewModelScope.launch {
                try {
                    // Firebase에 사진 업로드
                    val uploadedPhotoUrl = repository.uploadPhotoToFirebase(Uri.parse(currentState.photoUrl))

                    // DateRecord 객체 생성
                    val newRecord = DateRecord(
                        recordId = System.currentTimeMillis().toString(),
                        partnerA = RecordPart(
                            text = currentState.text,
                            photoUrls = listOf(uploadedPhotoUrl),
                            isComplete = true
                        ),
                        partnerB = RecordPart(text = "", photoUrls = emptyList(), isComplete = false),
                        missionStatus = MissionStatus.COMPLETED,
                        emotion = EmotionStatus.valueOf(currentState.emotion),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    // LocalDataStore에 저장
                    localDataStore.saveRecord(repository.dateRecordToEntity(newRecord))

                    // UI 상태 초기화 및 저장 완료 플래그 업데이트
                    _uiState.update { it.copy(isSaved = true, photoUrl = null, text = "", emotion = null) }
                } catch (e: Exception) {
                    e.printStackTrace() // 로그로 오류 확인
                }
            }
        }
    }
}

data class RecordUiState(
    val photoUrl: String? = null,
    val text: String = "",
    val emotion: String? = null,
    val isSaved: Boolean = false
)
