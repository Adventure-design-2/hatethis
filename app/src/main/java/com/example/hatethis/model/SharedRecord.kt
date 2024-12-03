package com.example.hatethis.model

import com.google.firebase.Timestamp

/**
 * 공유 가능한 기록 데이터를 정의하는 데이터 클래스.
 * @param id 고유 기록 ID
 * @param title 기록 제목
 * @param content 기록 내용
 * @param isShared 공유 여부
 * @param imageUrl 업로드된 이미지의 URL
 * @param createdAt 기록 생성 시간
 */
data class SharedRecord(
    val id: String = "", // 기록 ID (recordId -> id로 수정)
    val title: String = "", // 기록 제목
    val content: String = "", // 기록 내용
    val isShared: Boolean = false, // 공유 여부
    val imageUrl: String? = null, // 이미지 URL (null 허용)
    val createdAt: Timestamp = Timestamp.now() // 생성 시간
)
