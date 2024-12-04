package com.example.hatethis.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hatethis.data.DataRepository
import com.example.hatethis.viewmodel.AuthViewModel
import com.example.hatethis.viewmodel.MissionRecommendationViewModel
import com.example.hatethis.viewmodel.RecordListViewModel
import com.example.hatethis.viewmodel.RecordViewModel

class RecordViewModelFactory(
    private val dataRepository: DataRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            return RecordViewModel(dataRepository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RecordListViewModelFactory(
    private val dataRepository: DataRepository,
    private val authViewModel: AuthViewModel // 추가된 authViewModel 매개변수
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordListViewModel::class.java)) {
            return RecordListViewModel(dataRepository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



class MissionRecommendationViewModelFactory(
    private val dataRepository: DataRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MissionRecommendationViewModel::class.java)) {
            return MissionRecommendationViewModel(dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

