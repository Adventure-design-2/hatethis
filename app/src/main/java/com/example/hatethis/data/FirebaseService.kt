package com.example.hatethis.data

import android.net.Uri
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.MissionStatus
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseService {
    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val realtimeDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }


    /**
     * Realtime Database에 기록 저장
     * @param recordMap Map<String, Any> 객체
     * @param recordId 기록의 고유 ID
     */
    suspend fun saveRecordToRealtimeDatabase(recordMap: Map<String, Any?>, recordId: String) {
        try {
            // `comments` 필드가 추가된 데이터 구조로 저장
            realtimeDatabase.child("records").child(recordId).setValue(recordMap).await()
        } catch (e: Exception) {
            throw IllegalStateException("Realtime Database 기록 저장 실패: ${e.message}", e)
        }
    }


    /**
     * Realtime Database에서 기록 가져오기
     * @return 기록의 리스트
     */
    suspend fun getRecordsFromRealtimeDatabase(): List<DateRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = realtimeDatabase.child("records").get().await()
                snapshot.children.mapNotNull { child ->
                    val record = child.value as? Map<*, *> ?: return@mapNotNull null
                    DateRecord(
                        recordId = record["recordId"] as? String ?: "",
                        partnerA = record["partnerA"] as? String ?: "",
                        partnerB = record["partnerB"] as? String ?: "",
                        missionStatus = (record["missionStatus"] as? String)?.let {
                            MissionStatus.valueOf(it) // 문자열을 MissionStatus로 변환
                        } ?: MissionStatus.NOT_STARTED,
                        emotion = (record["emotion"] as? String)?.let {
                            EmotionStatus.valueOf(it) // 문자열을 EmotionStatus로 변환
                        },
                        photoUrls = record["photoUrls"] as? List<String> ?: emptyList(),
                        comments = record["comments"] as? List<String> ?: emptyList(),
                        createdAt = record["createdAt"] as? Long ?: 0L,
                        updatedAt = record["updatedAt"] as? Long ?: 0L
                    )
                }
            } catch (e: Exception) {
                throw IllegalStateException("Realtime Database 기록 가져오기 실패: ${e.message}", e)
            }
        }
    }



    /**
     * 사진 업로드
     * @param fileUri 사진의 Uri
     * @param folderPath Firebase Storage에 저장할 폴더 경로
     * @return 업로드된 사진의 다운로드 URL
     */
    suspend fun uploadPhoto(fileUri: Uri, folderPath: String): String {
        require(folderPath.isNotBlank()) { "폴더 경로는 비어 있을 수 없습니다." }

        try {
            val fileName = UUID.randomUUID().toString()
            val fileReference = storageReference.child("$folderPath/$fileName")
            fileReference.putFile(fileUri).await()
            return fileReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw IllegalStateException("사진 업로드 실패: ${e.message}", e)
        }
    }

    /**
     * 사진 삭제
     * @param photoUrl 삭제할 사진의 다운로드 URL
     */
    suspend fun deletePhoto(photoUrl: String) {
        try {
            val fileReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
            fileReference.delete().await()
        } catch (e: Exception) {
            throw IllegalStateException("사진 삭제 실패: ${e.message}", e)
        }
    }

    suspend fun getMissionsFromFirestore(): List<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            val snapshot = Firebase.firestore.collection("missions").get().await()
            snapshot.documents.mapNotNull { it.data }
        }
    }

}
