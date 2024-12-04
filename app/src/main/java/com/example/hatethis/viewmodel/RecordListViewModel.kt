package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.model.DateRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordListViewModel(
    private val repository: DataRepository,
    private val authViewModel: AuthViewModel // 사용자 정보를 가져오기 위한 뷰모델
) : ViewModel() {

    // 상태 관리
    private val _records = MutableStateFlow<List<DateRecord>>(emptyList())
    val records: StateFlow<List<DateRecord>> = _records

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadRecords()
    }

    fun loadRecords() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val userUid = authViewModel.getCurrentUserId()

                // Realtime Database에서 사용자 기록 가져오기
                val userRecords = repository.getRecordsForUser(userUid)

                _records.value = userRecords
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "기록 불러오기 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
