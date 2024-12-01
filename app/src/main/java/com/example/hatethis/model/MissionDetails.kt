package com.example.hatethis.model

data class MissionDetails(
    val title: String = "",       // 미션 제목
    val description: String = "", // 미션 설명
    val createdAt: Long = 0L      // 생성 시간 (타임스탬프)
)
