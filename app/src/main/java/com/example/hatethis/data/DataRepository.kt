package com.example.hatethis.data

import android.net.Uri
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.MissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataRepository(
    private val firebaseService: FirebaseService,
    private val localDataStore: LocalDataStore
) {
    suspend fun uploadPhotoToFirebase(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            firebaseService.uploadPhoto(uri, "date-records")
        }
    }

    private suspend fun saveRecordToLocal(dateRecord: DateRecord) {
        withContext(Dispatchers.IO) {
            localDataStore.saveRecord(dateRecordToEntity(dateRecord))
        }
    }

    internal suspend fun saveRecordToRealtimeDatabase(dateRecord: DateRecord) {
        val recordMap = mapOf(
            "recordId" to dateRecord.recordId,
            "partnerA" to dateRecord.partnerA,
            "partnerB" to dateRecord.partnerB,
            "missionStatus" to dateRecord.missionStatus.name, // Enum을 문자열로 저장
            "emotion" to dateRecord.emotion?.name, // Optional Enum
            "photoUrls" to dateRecord.photoUrls, // List<String> 그대로 저장
            "comments" to dateRecord.comments, // List<String> 그대로 저장
            "createdAt" to dateRecord.createdAt, // Long 타입의 타임스탬프
            "updatedAt" to dateRecord.updatedAt  // Long 타입의 타임스탬프
        )

        try {
            // Realtime Database에 데이터를 저장
            firebaseService.saveRecordToRealtimeDatabase(recordMap, dateRecord.recordId)
        } catch (e: Exception) {
            // 예외 처리 및 로그 출력
            throw IllegalStateException("Realtime Database 기록 저장 실패: ${e.message}", e)
        }
    }


    suspend fun getRecordsFromRealtimeDatabase(): List<DateRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firebaseService.getRecordsFromRealtimeDatabase()
                snapshot.mapNotNull { rawRecord ->
                    // 명시적 null 검사 및 데이터 파싱
                    val recordMap = rawRecord as? Map<String, Any> ?: return@mapNotNull null

                    val photoUrls = (recordMap["photoUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val comments = (recordMap["comments"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    DateRecord(
                        recordId = recordMap["recordId"] as? String ?: "",
                        partnerA = recordMap["partnerA"] as? String ?: "",
                        partnerB = recordMap["partnerB"] as? String ?: "",
                        missionStatus = (recordMap["missionStatus"] as? String)?.let { enumValueOf<MissionStatus>(it) }
                            ?: MissionStatus.NOT_STARTED,
                        emotion = (recordMap["emotion"] as? String)?.let { EmotionStatus.valueOf(it) },
                        photoUrls = photoUrls,
                        comments = comments,
                        createdAt = recordMap["createdAt"] as? Long ?: 0L,
                        updatedAt = recordMap["updatedAt"] as? Long ?: 0L
                    )
                }
            } catch (e: Exception) {
                throw IllegalStateException("Realtime Database 데이터 로드 실패: ${e.message}", e)
            }
        }
    }



    suspend fun saveRecord(dateRecord: DateRecord) {
        withContext(Dispatchers.IO) {
            try {
                saveRecordToLocal(dateRecord)
                saveRecordToRealtimeDatabase(dateRecord)
            } catch (e: Exception) {
                throw IllegalStateException("기록 저장 실패: ${e.message}", e)
            }
        }
    }

    suspend fun getAllLocalRecords(): List<DateRecord> {
        return withContext(Dispatchers.IO) {
            localDataStore.getAllRecords().map { entityToDateRecord(it) }
        }
    }

    suspend fun getLocalRecordById(recordId: String): DateRecord? {
        return withContext(Dispatchers.IO) {
            localDataStore.getRecordById(recordId)?.let { entityToDateRecord(it) }
        }
    }

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
}
