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

    // 로컬 기록 관리
    suspend fun saveRecordToLocal(dateRecord: DateRecord) {
        withContext(Dispatchers.IO) {
            localDataStore.saveRecord(dateRecordToEntity(dateRecord))
        }
    }

    suspend fun getAllLocalRecords(): List<DateRecord> {
        return withContext(Dispatchers.IO) {
            localDataStore.getAllRecords().map { entityToDateRecord(it) }
        }
    }

    // 서버에서 미션 가져오기
    suspend fun fetchMissionsFromServer(): List<Mission> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firebaseService.getMissionsFromFirestore()
                snapshot.mapNotNull { rawMission ->
                    val missionMap = rawMission as? Map<String, Any> ?: return@mapNotNull null
                    Mission(
                        title = missionMap["title"] as? String ?: "",
                        environment = (missionMap["environment"] as? Long)?.toInt() ?: 0,
                        locationTag = (missionMap["locationTag"] as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        detail = missionMap["detail"] as? String ?: ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // 로컬 미션 업데이트
    suspend fun updateLocalMissions(missions: List<Mission>) {
        withContext(Dispatchers.IO) {
            localDataStore.saveMissions(missions.map { missionToEntity(it) })
        }
    }

    // 로컬 미션 가져오기
    suspend fun getLocalMissions(): List<Mission> {
        return withContext(Dispatchers.IO) {
            localDataStore.getAllMissions().map { entityToMission(it) }
        }
    }

    // 헬퍼 메서드
    private fun dateRecordToEntity(dateRecord: DateRecord): DateRecordEntity {
        return DateRecordEntity(
            recordId = dateRecord.recordId,
            partnerA = dateRecord.partnerA,
            partnerB = dateRecord.partnerB,
            missionStatus = dateRecord.missionStatus.name,
            emotion = dateRecord.emotion?.name,
            photoUrls = dateRecord.photoUrls.joinToString(","),
            comments = dateRecord.comments.joinToString(","),
            createdAt = dateRecord.createdAt,
            updatedAt = dateRecord.updatedAt
        )
    }

    private fun entityToDateRecord(entity: DateRecordEntity): DateRecord {
        return DateRecord(
            recordId = entity.recordId,
            partnerA = entity.partnerA,
            partnerB = entity.partnerB,
            missionStatus = enumValueOf(entity.missionStatus),
            emotion = entity.emotion?.let { EmotionStatus.valueOf(it) },
            photoUrls = entity.photoUrls.split(",").filter { it.isNotBlank() },
            comments = entity.comments.split(",").filter { it.isNotBlank() },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

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

    suspend fun updateMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            val missionEntity = missionToEntity(mission)
            localDataStore.updateMission(missionEntity) // 로컬 데이터 업데이트
        }
    }

}
