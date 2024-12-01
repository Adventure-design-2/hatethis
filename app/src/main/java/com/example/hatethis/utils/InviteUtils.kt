package com.example.hatethis.utils

import kotlin.random.Random

object InviteUtils {
    private const val CODE_LENGTH = 12
    private const val CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    // 12자 길이의 랜덤 초대 코드 생성
    fun generateInviteCode(): String {
        return (1..CODE_LENGTH)
            .map { Random.nextInt(0, CHAR_POOL.length) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }
}
