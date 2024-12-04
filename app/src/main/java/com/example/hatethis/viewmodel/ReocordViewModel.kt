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
import java.util.UUID

class RecordViewModel(
    private val repository: DataRepository,
    private val authViewModel: AuthViewModel // AuthViewModel 주입
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
            _uiState.update { it.copy(uploadStatus = UploadStatus.UPLOADING) }

            viewModelScope.launch {
                try {
                    // 사용자와 파트너 ID 가져오기
                    val currentUserId = authViewModel.getCurrentUserId()
                    val partnerId = authViewModel.fetchPartnerId()

                    if (currentUserId.isEmpty() || partnerId.isNullOrEmpty()) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "사용자 또는 파트너 ID를 찾을 수 없습니다.",
                                uploadStatus = UploadStatus.FAILURE
                            )
                        }
                        return@launch
                    }

                    // 사진 업로드
                    val uploadedPhotoUrl = repository.uploadPhotoToFirebase(Uri.parse(currentState.photoUrl))

                    // UUID를 사용하여 고유한 recordId 생성
                    val recordId = UUID.randomUUID().toString()

                    // 새로운 DateRecord 객체 생성
                    val newRecord = DateRecord(
                        recordId = recordId,
                        partnerA = currentUserId,
                        partnerB = partnerId,
                        missionStatus = MissionStatus.COMPLETED,
                        emotion = currentState.emotion.let { EmotionStatus.valueOf(it) },
                        photoUrls = listOf(uploadedPhotoUrl),
                        comments = listOf(currentState.text), // `text`가 `comments`에 제대로 들어가는지 확인
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )


                    // 기록 저장
                    repository.saveRecordToRealtimeDatabase(newRecord)
                    _uiState.update {
                        it.copy(
                            isSaved = true,
                            uploadStatus = UploadStatus.SUCCESS,
                            errorMessage = null
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update {
                        it.copy(
                            errorMessage = "기록 저장 실패: ${e.message}",
                            uploadStatus = UploadStatus.FAILURE
                        )
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = "모든 필드를 입력해야 합니다.",
                    uploadStatus = UploadStatus.FAILURE
                )
            }
        }
    }

    fun loadRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // 로드 시작
            try {
                val currentUserId = authViewModel.getCurrentUserId()
                val records = repository.getRecordsForUser(currentUserId)

                _uiState.update {
                    it.copy(
                        records = records,
                        isLoading = false, // 로드 완료
                        uploadStatus = UploadStatus.SUCCESS,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        errorMessage = "기록 불러오기 실패: ${e.message}",
                        isLoading = false, // 로드 실패
                        uploadStatus = UploadStatus.FAILURE
                    )
                }
            }
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
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val isLoading: Boolean = false, // 로드 상태 추가
    val records: List<DateRecord> = emptyList()
)
