package com.example.hatethis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hatethis.model.InviteData
import com.example.hatethis.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 로그인 상태 관리
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    init {
        checkLoginStatus() // 초기 로그인 상태 확인
    }

    // 회원가입 로직
    fun registerUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true) // 회원가입 성공
                } else {
                    onResult(false) // 회원가입 실패
                }
            }
    }

    // 로그인 로직
    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkLoginStatus() // 로그인 상태 업데이트
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
    }

    fun signInWithEmailAndPassword(email: String, password: String, onResult: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkLoginStatus() // 로그인 상태 업데이트
                    onResult(true) // 로그인 성공
                } else {
                    onResult(false) // 로그인 실패
                }
            }
    }

    // 현재 사용자 UID 반환
    fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    // 유저 프로필 저장
    fun saveUserProfile(profile: UserProfile, onResult: (Boolean) -> Unit) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .set(profile)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        } else {
            onResult(false)
        }
    }

    // 유저 프로필 로드
    fun loadUserProfile(onResult: (UserProfile?) -> Unit) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        onResult(profile)
                    } else {
                        onResult(null)
                    }
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }

    // 초대 코드 저장
    suspend fun saveInviteCode(uid: String, inviteCode: String): Boolean {
        return try {
            val expirationTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 24시간 후 만료
            val inviteData = InviteData(
                uid = uid,
                inviteCode = inviteCode,
                expirationTime = expirationTime
            )

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

    // 만료된 초대 코드 삭제
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

    // 초대 코드로 파트너 연결
    suspend fun connectPartner(userUid: String, partnerCode: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("inviteCode", partnerCode)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                return false
            }

            val partnerUid = snapshot.documents[0].id

            // Firestore에 파트너 정보 업데이트
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


    // 로그아웃
    fun logout() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
    }

    // 로그인 상태 확인
    private fun checkLoginStatus() {
        _isLoggedIn.value = firebaseAuth.currentUser != null
    }

    suspend fun generateUniqueInviteCode(): String {
        var isUnique = false
        var newCode: String

        do {
            newCode = com.example.hatethis.utils.InviteUtils.generateInviteCode()
            val existingCode = firestore.collection("users")
                .whereEqualTo("inviteCode", newCode)
                .get()
                .await()

            if (existingCode.isEmpty) {
                isUnique = true
            }
        } while (!isUnique)

        return newCode
    }
    suspend fun updateInviteCode(userUid: String, inviteCode: String): Boolean {
        return try {
            firestore.collection("users").document(userUid)
                .update("inviteCode", inviteCode)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
