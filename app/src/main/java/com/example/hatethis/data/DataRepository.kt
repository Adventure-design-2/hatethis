package com.example.hatethis.data

import android.net.Uri
import com.example.hatethis.model.DateRecord
import com.example.hatethis.model.EmotionStatus
import com.example.hatethis.model.RecordPart
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DataRepository(
    private val firebaseService: FirebaseService,
    private val localDataStore: LocalDataStore
) {
    private val gson = Gson()

    private fun parseRecordPart(json: String): RecordPart {
        return gson.fromJson(json, RecordPart::class.java)
    }

    private fun toJson(recordPart: RecordPart): String {
        return gson.toJson(recordPart)
    }

    private fun entityToDateRecord(entity: DateRecordEntity): DateRecord {
        return DateRecord(
            recordId = entity.recordId,
            partnerA = parseRecordPart(entity.partnerA),
            partnerB = parseRecordPart(entity.partnerB),
            missionStatus = enumValueOf(entity.missionStatus),
            emotion = entity.emotion?.let { EmotionStatus.valueOf(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    internal fun dateRecordToEntity(dateRecord: DateRecord): DateRecordEntity {
        return DateRecordEntity(
            recordId = dateRecord.recordId,
            partnerA = toJson(dateRecord.partnerA),
            partnerB = toJson(dateRecord.partnerB),
            missionStatus = dateRecord.missionStatus.name,
            emotion = dateRecord.emotion?.name,
            createdAt = dateRecord.createdAt,
            updatedAt = dateRecord.updatedAt
        )
    }

    suspend fun uploadPhotoToFirebase(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            firebaseService.uploadPhoto(uri, "date-records")
        }
    }

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

    suspend fun getLocalRecordById(recordId: String): DateRecord? {
        return withContext(Dispatchers.IO) {
            localDataStore.getRecordById(recordId)?.let { entityToDateRecord(it) }
        }
    }
}
