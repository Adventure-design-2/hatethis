package com.example.hatethis.model

// DateRecord: 데이트 기록 데이터를 관리하는 데이터 클래스
data class DateRecord(
    val recordId: String,       // 기록 고유 ID
    val partnerA: RecordPart,   // 파트너 A의 데이터
    val partnerB: RecordPart,   // 파트너 B의 데이터
    val missionStatus: MissionStatus, // 미션 상태
    val emotion: EmotionStatus?,      // 기록된 감정 상태 (Optional)
    val createdAt: Long,       // 생성 시간 (타임스탬프)
    val updatedAt: Long        // 마지막 업데이트 시간 (타임스탬프)
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

// EmotionStatus: 감정을 나타내는 열거형
enum class EmotionStatus {
    HAPPY,   // 행복
    SAD,     // 슬픔
    NEUTRAL, // 중립
    ANGRY    // 화남
}
