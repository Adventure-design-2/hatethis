package com.example.hatethis.model

data class InviteData(
    val uid: String = "",          // 초대 코드를 생성한 사용자 ID
    val inviteCode: String = "",   // 생성된 초대 코드
    val expirationTime: Long = 0L  // 초대 코드 만료 시간 (timestamp)
)
