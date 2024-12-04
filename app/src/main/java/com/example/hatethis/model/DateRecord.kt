package com.example.hatethis.model

/**
 * DateRecord: 데이트 기록 데이터를 관리하는 데이터 클래스
 *
 * @property recordId 기록 고유 ID
 * @property partnerA 파트너 A의 사용자 ID (로그인한 사용자 ID)
 * @property partnerB 파트너 B의 사용자 ID (상대방 사용자 ID)
 * @property missionStatus 미션 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED)
 * @property emotion 기록된 감정 상태 (HAPPY, SAD, NEUTRAL 등) (Optional)
 * @property photoUrls 사진 URL 리스트 (Firebase Storage에 업로드된 URL 목록)
 * @property comments 댓글 목록 (각각의 댓글은 문자열 형태)
 * @property createdAt 생성 시간 (타임스탬프, UTC 기준 밀리초)
 * @property updatedAt 마지막 업데이트 시간 (타임스탬프, UTC 기준 밀리초)
 */
data class DateRecord(
    val recordId: String,              // 기록 고유 ID
    val partnerA: String,              // 파트너 A의 사용자 ID
    val partnerB: String,              // 파트너 B의 사용자 ID
    val missionStatus: MissionStatus,  // 미션 상태
    val emotion: EmotionStatus?,       // 기록된 감정 상태 (Optional)
    val photoUrls: List<String>,       // 사진 URL 리스트
    val comments: List<String> = emptyList(), // 댓글 리스트 (Optional, 기본값: 빈 리스트)
    val createdAt: Long,               // 생성 시간 (타임스탬프)
    val updatedAt: Long                // 마지막 업데이트 시간 (타임스탬프)
)

// RecordPart: 각 파트너의 기록 데이터를 관리하는 서브 클래스
data class RecordPart(
    val text: String?,          // 텍스트 입력 (Optional)
    val photoUrls: List<String>, // Firebase Storage에 업로드된 사진 URL 목록
    val isComplete: Boolean     // 입력 완료 여부
)

// MissionStatus: 미션 상태를 나타내는 열거형
enum class MissionStatus {
    NOT_STARTED, // 미션 시작 전
    IN_PROGRESS, // 미션 진행 중
    COMPLETED    // 미션 완료
}

enum class EmotionStatus {
    Happy,
    Sad,
    Neutral;

    companion object {
        fun String.toEmotionStatus(): EmotionStatus? {
            return try {
                EmotionStatus.valueOf(this.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}



