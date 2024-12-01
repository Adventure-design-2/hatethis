package com.example.hatethis.model

data class MissionRecord(
    val text: String = "",        // 기록 내용
    val imageUrl: String = "",    // 업로드된 이미지 URL
    val completedAt: Long = 0L    // 완료 시간 (타임스탬프)
)
