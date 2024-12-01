package com.example.hatethis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hatethis.model.Mission
import com.example.hatethis.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 로그인 상태 관리
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> get() = _missions

    init {
        checkLoginStatus() // 초기 로그인 상태 확인
    }

    // 회원가입 로직
    fun registerUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Google 로그인
    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) checkLoginStatus()
                onResult(task.isSuccessful)
            }
    }

    // 이메일 및 비밀번호 로그인
    fun signInWithEmailAndPassword(email: String, password: String, onResult: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) checkLoginStatus()
                onResult(task.isSuccessful)
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
                .addOnSuccessListener {
                    Log.d("Firestore", "유저 프로필 저장 성공")
                    onResult(true)
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "유저 프로필 저장 실패: ${exception.localizedMessage}")
                    onResult(false)
                }
        } else {
            Log.e("Firestore", "유저 인증되지 않음")
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
                        onResult(null) // 프로필 데이터 없음
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "유저 프로필 로드 실패")
                    onResult(null)
                }
        } else {
            onResult(null)
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

    fun loadUserProfileByUid(userUid: String, onResult: (UserProfile?) -> Unit) {
        firestore.collection("users").document(userUid)
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
    }

}
