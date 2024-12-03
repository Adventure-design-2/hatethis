package com.example.hatethis.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val bio: String = "",
    val imageUrl: String = "",
    val partnerUid: String = "", // 파라미터 필드 추가
    val inviteCode: String = "" // 초대 코드 필드 추가
)
