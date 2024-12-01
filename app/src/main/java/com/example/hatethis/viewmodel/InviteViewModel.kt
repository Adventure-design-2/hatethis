package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hatethis.model.InviteData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InviteViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * 초대 코드 저장
     */
    suspend fun saveInviteCode(uid: String, inviteCode: String): Boolean {
        return try {
            val expirationTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 24시간 후 만료
            val inviteData = InviteData(
                uid = uid,
                inviteCode = inviteCode,
                expirationTime = expirationTime
            )

            // Firestore에 InviteData 저장
            firestore.collection("invites")
                .document(uid)
                .set(inviteData)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 만료된 초대 코드 삭제
     */
    suspend fun deleteExpiredInviteCodes() {
        try {
            val currentTime = System.currentTimeMillis()
            val snapshot = firestore.collection("invites")
                .whereLessThanOrEqualTo("expirationTime", currentTime)
                .get()
                .await()

            val expiredInvites = snapshot.toObjects(InviteData::class.java)
            for (invite in expiredInvites) {
                firestore.collection("invites").document(invite.uid).delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 초대 코드로 파트너 연결
     */
    suspend fun connectPartner(userUid: String, partnerCode: String): Boolean {
        return try {
            val snapshot = firestore.collection("invites")
                .whereEqualTo("inviteCode", partnerCode)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                return false
            }

            val invite = snapshot.documents[0].toObject(InviteData::class.java) ?: return false
            val partnerUid = invite.uid

            // 파트너 연결
            firestore.collection("users").document(userUid)
                .update("partnerUid", partnerUid)
                .await()

            firestore.collection("users").document(partnerUid)
                .update("partnerUid", userUid)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
