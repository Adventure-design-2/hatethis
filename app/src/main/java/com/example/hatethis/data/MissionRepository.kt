package com.example.hatethis.data

import android.content.Context
import com.example.hatethis.model.Mission
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MissionRepository(private val context: Context) {
    private val fileName = "missions.json"
    private val gson = Gson()

    // 로컬 JSON 파일 경로
    private val file: File
        get() = File(context.filesDir, fileName)

    // 모든 미션 로드
    fun loadMissions(): List<Mission> {
        return if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<Mission>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // 미션 저장
    fun saveMissions(missions: List<Mission>) {
        val json = gson.toJson(missions)
        file.writeText(json)
    }

    // 추천 미션 가져오기
    fun getRecommendedMission(): Mission? {
        val missions = loadMissions()
        return missions
            .filter { it.completedCount == 0 || it.environment > 0 } // 조건 추가
            .sortedWith(compareByDescending<Mission> { it.environment }.thenBy { it.completedCount })
            .firstOrNull()
    }
}
