package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatethis.data.DataRepository
import com.example.hatethis.model.DateRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecordListViewModel(private val repository: DataRepository) : ViewModel() {
    private val _records = MutableStateFlow<List<DateRecord>>(emptyList())
    val records: StateFlow<List<DateRecord>> = _records

    init {
        loadRecords()
    }

    /**
     * Realtime Database에서 기록을 불러옵니다.
     */
    fun loadRecords() {
        viewModelScope.launch {
            try {
                // Realtime Database에서 기록 가져오기
                val realtimeRecords = repository.getRecordsFromRealtimeDatabase()
                _records.value = realtimeRecords
            } catch (e: Exception) {
                e.printStackTrace() // 오류 출력
                _records.value = emptyList() // 오류 발생 시 빈 리스트로 초기화
            }
        }
    }
}
