package com.example.hatethis.data

import android.net.Uri
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.Mission
import com.example.hatethis.model.MissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataRepository(
    private val firebaseService: FirebaseService,
    private val localDataStore: LocalDataStore
) {

    // 사진 업로드
    suspend fun uploadPhotoToFirebase(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            firebaseService.uploadPhoto(uri, "date-records")
        }
    }

    // 서버에 기록 저장
    internal suspend fun saveRecordToRealtimeDatabase(dateRecord: DateRecord) {
        val recordMap = mapOf(
            "recordId" to dateRecord.recordId,
            "partnerA" to dateRecord.partnerA,
            "partnerB" to dateRecord.partnerB,
            "missionStatus" to dateRecord.missionStatus.name,
            "emotion" to dateRecord.emotion?.name,
            "photoUrls" to dateRecord.photoUrls,
            "comments" to dateRecord.comments,
            "createdAt" to dateRecord.createdAt,
            "updatedAt" to dateRecord.updatedAt
        )
        try {
            firebaseService.saveRecordToRealtimeDatabase(recordMap, dateRecord.recordId)
        } catch (e: Exception) {
            throw IllegalStateException("Realtime Database 기록 저장 실패: ${e.message}", e)
        }
    }

    // 사용자의 모든 기록 가져오기
    suspend fun getRecordsForUser(userUid: String): List<DateRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firebaseService.getRecordsFromRealtimeDatabase()
                snapshot.mapNotNull { rawRecord ->
                    val recordMap = rawRecord as? Map<String, Any> ?: return@mapNotNull null
                    val partnerA = recordMap["partnerA"] as? String
                    val partnerB = recordMap["partnerB"] as? String
                    if (partnerA != userUid && partnerB != userUid) return@mapNotNull null

                    DateRecord(
                        recordId = recordMap["recordId"] as? String ?: "",
                        partnerA = partnerA ?: "",
                        partnerB = partnerB ?: "",
                        missionStatus = (recordMap["missionStatus"] as? String)?.let {
                            MissionStatus.valueOf(it)
                        } ?: MissionStatus.NOT_STARTED,
                        emotion = (recordMap["emotion"] as? String)?.let {
                            EmotionStatus.valueOf(it)
                        },
                        photoUrls = (recordMap["photoUrls"] as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        comments = (recordMap["comments"] as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        createdAt = recordMap["createdAt"] as? Long ?: 0L,
                        updatedAt = recordMap["updatedAt"] as? Long ?: 0L
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // 삭제된 코드: Firestore에서 미션 가져오기
    /*
    suspend fun fetchMissionsFromServer(): List<Mission> {
        // Firebase에서 미션 데이터를 가져오는 기능은 삭제됨
        // 이유: 미션은 서버에서 동적으로 가져오지 않고, 앱 다운로드 시 로컬 저장소에 저장되므로 필요 없음
        return withContext(Dispatchers.IO) { emptyList() }
    }
    */

    // 삭제된 코드: 로컬 미션 업데이트
    /*
    suspend fun updateLocalMissions(missions: List<Mission>) {
        // 이 함수는 외부에서 동적으로 미션 데이터를 업데이트할 필요가 없으므로 삭제
        // 이유: 모든 미션은 초기 설치 시 저장되며, 앱 내에서 상태만 변경됨
    }
    */

    // 기본 미션 데이터 삽입
    suspend fun initializeMissionsIfNeeded(defaultMissions: List<Mission>) {
        withContext(Dispatchers.IO) {
            val existingMissions = localDataStore.getAllMissions()
            if (existingMissions.isEmpty()) {
                // 기본 미션 데이터를 로컬 저장소에 저장
                localDataStore.saveMissions(defaultMissions.map { missionToEntity(it) })
                println("Default missions initialized.")
            }
        }
    }

    // 추천 알고리즘
    fun getRecommendedMissions(missions: List<Mission>): List<Mission> {
        return missions.sortedByDescending { mission ->
            calculateRecommendationScore(mission)
        }.take(5) // 상위 5개의 미션만 추천
    }

    // 추천 점수 계산 로직
    private fun calculateRecommendationScore(mission: Mission): Double {
        return (mission.environment + 1).toDouble() / (1 + mission.completedCount)
    }

    // 로컬 미션 데이터 가져오기
    suspend fun getLocalMissions(): List<Mission> {
        return withContext(Dispatchers.IO) {
            localDataStore.getAllMissions().map { entityToMission(it) }
        }
    }

    // 로컬 미션 데이터 업데이트
    suspend fun updateMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            val missionEntity = missionToEntity(mission)
            localDataStore.updateMission(missionEntity)
        }
    }

    // 헬퍼 메서드
    private fun missionToEntity(mission: Mission): MissionEntity {
        return MissionEntity(
            title = mission.title,
            environment = mission.environment,
            locationTag = mission.locationTag.joinToString(","),
            detail = mission.detail
        )
    }

    private fun entityToMission(entity: MissionEntity): Mission {
        return Mission(
            title = entity.title,
            environment = entity.environment,
            locationTag = entity.locationTag.split(",").filter { it.isNotBlank() },
            detail = entity.detail
        )
    }
}
