package com.example.hatethis.model

data class Mission(
    val title: String,
    val environment: Int,
    val locationTag: List<String>,
    val detail: String,
    var completedCount: Int = 0 // 완료 횟수 필드 추가
)
