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

    private val _sharedRecords = MutableStateFlow<List<SharedRecord>>(emptyList())
    val sharedRecords: StateFlow<List<SharedRecord>> get() = _sharedRecords

    suspend fun saveRecord(author: String, content: String, onResult: (Boolean) -> Unit) {
        try {
            val recordId = UUID.randomUUID().toString()
            val newRecord = SharedRecord(
                id = recordId,
                title = "기본 제목",
                content = content,
                isShared = false,
                createdAt = Timestamp.now()
            )

            firestore.collection("records")
                .document(recordId)
                .set(newRecord)
                .await()

            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }

    suspend fun loadSharedRecords(onResult: (List<SharedRecord>) -> Unit) {
        try {
            val snapshot = firestore.collection("records")
                .whereEqualTo("isShared", true)
                .get()
                .await()

            val sharedRecords = snapshot.toObjects(SharedRecord::class.java)
            _sharedRecords.update { sharedRecords }
            onResult(sharedRecords)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(emptyList())
        }
    }

    suspend fun fetchRecords() {
        try {
            val snapshot = firestore.collection("records").get().await()
            val fetchedRecords = snapshot.documents.mapNotNull { document ->
                document.toObject(SharedRecord::class.java)?.copy(id = document.id)
            }
            _records.update { fetchedRecords }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

    suspend fun saveRecordWithImage(
        title: String,
        content: String,
        imageUri: Uri?,
        onResult: (Boolean) -> Unit
    ) {
        try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).await()
                imageUrl = imageRef.downloadUrl.await().toString()
            }

            val newRecord = SharedRecord(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                imageUrl = imageUrl,
                isShared = false,
                createdAt = Timestamp.now()
            )

            firestore.collection("records").document(newRecord.id).set(newRecord).await()
            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }
}
