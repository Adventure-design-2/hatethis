package com.example.hatethis.model

data class Mission(
    val missionId: String = "",              // 미션 ID
    val participants: List<String> = emptyList(), // 참여자 UID 리스트
    val missionDetails: MissionDetails = MissionDetails(), // 미션 기본 정보
    val records: Map<String, MissionRecord> = emptyMap()   // 참여자별 기록
)
