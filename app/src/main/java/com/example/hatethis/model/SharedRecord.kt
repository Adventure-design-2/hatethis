package com.example.hatethis.model

import com.google.firebase.Timestamp

data class SharedRecord(
    val id: String = "", // 기록 ID
    val title: String = "", // 제목
    val content: String = "", // 내용
    val isShared: Boolean = false, // 공유 여부
    val imageUrl: String? = null, // 이미지 URL
    val authorIds: List<String> = emptyList(), // 작성자 ID 리스트
    val createdAt: Timestamp = Timestamp.now() // 생성 시간
)

