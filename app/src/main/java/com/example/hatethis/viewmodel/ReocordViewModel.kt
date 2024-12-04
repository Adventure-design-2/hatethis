package com.example.hatethis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.MissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: DataRepository) : ViewModel() {

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
            _uiState.update { it.copy(uploadStatus = UploadStatus.UPLOADING) }

            viewModelScope.launch {
                try {
                    // Firebase Storage에 사진 업로드
                    val uploadedPhotoUrl = repository.uploadPhotoToFirebase(Uri.parse(currentState.photoUrl))

                    // 새로운 DateRecord 객체 생성
                    val newRecord = DateRecord(
                        recordId = System.currentTimeMillis().toString(),
                        partnerA = currentState.text, // 파트너 A의 ID
                        partnerB = "", // 파트너 B의 ID는 빈 값으로 설정
                        missionStatus = MissionStatus.COMPLETED,
                        emotion = currentState.emotion.let { EmotionStatus.valueOf(it) },
                        photoUrls = listOf(uploadedPhotoUrl),
                        comments = emptyList(), // 초기에는 댓글이 없음
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    // Realtime Database에 기록 저장
                    repository.saveRecordToRealtimeDatabase(newRecord)
                    _uiState.update { RecordUiState(isSaved = true, uploadStatus = UploadStatus.SUCCESS) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to save record.",
                            uploadStatus = UploadStatus.FAILURE
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(errorMessage = "All fields must be filled.") }
        }
    }
}

enum class UploadStatus {
    IDLE, UPLOADING, SUCCESS, FAILURE
}

data class RecordUiState(
    val photoUrl: String? = null,
    val text: String = "",
    val emotion: String? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val uploadStatus: UploadStatus = UploadStatus.IDLE // 업로드 상태
)
