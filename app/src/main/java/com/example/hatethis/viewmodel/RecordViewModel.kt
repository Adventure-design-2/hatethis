package com.example.hatethis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.hatethis.model.SharedRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.*

class RecordViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage

    private val _records = MutableStateFlow<List<SharedRecord>>(emptyList())
    val records: StateFlow<List<SharedRecord>> get() = _records

    /**
     * 연결된 파트너 UID 가져오기
     */
    suspend fun getConnectedPartnerId(userUid: String): String? {
        return try {
            val userDoc = firestore.collection("users").document(userUid).get().await()
            userDoc.getString("connectedPartnerId")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 기록 저장
     */
    suspend fun saveRecordWithImage(
        title: String,
        content: String,
        imageUri: Uri?,
        userUid: String,
        onResult: (Boolean) -> Unit
    ) {
        try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).await()
                imageUrl = imageRef.downloadUrl.await().toString()
            }

            val partnerUid = getConnectedPartnerId(userUid)
            val authorIds = listOfNotNull(userUid, partnerUid) // 작성자와 파트너 UID 리스트 생성

            val newRecord = SharedRecord(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                imageUrl = imageUrl,
                isShared = false,
                authorIds = authorIds, // 새 authorIds 필드에 저장
                createdAt = Timestamp.now()
            )

            firestore.collection("records").document(newRecord.id).set(newRecord).await()
            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }

    /**
     * 연결된 사용자와 공유된 기록만 불러오기
     */
    suspend fun fetchSharedRecords(userUid: String) {
        try {
            println("Fetching records for user: $userUid")
            val snapshot = firestore.collection("records")
                .whereArrayContains("authorIds", userUid) // authorIds 배열에 userUid 포함 여부로 검색
                .get()
                .await()

            val fetchedRecords = snapshot.documents.mapNotNull { document ->
                document.toObject(SharedRecord::class.java)?.copy(id = document.id)
            }
            println("Fetched records: $fetchedRecords")
            _records.update { fetchedRecords }
        } catch (e: Exception) {
            println("Error fetching records: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    /**
     * 기록 공유 상태 업데이트
     */
    suspend fun updateRecordShareStatus(recordId: String, isShared: Boolean, onComplete: (Boolean) -> Unit) {
        try {
            firestore.collection("records").document(recordId)
                .update("isShared", isShared)
                .await()

            onComplete(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false)
        }
    }
}
