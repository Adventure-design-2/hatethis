package com.example.hatethis

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hatethis.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // 레이아웃 연결

        // ViewModel 초기화
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // GoogleSignInClient 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // Deprecated 경고 있음
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google 로그인 버튼 클릭 처리
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                authViewModel.signInWithGoogle(account.idToken!!) { success ->
                    if (success) {
                        // 유저에게 성공 메시지 표시
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    } else {
                        // 유저에게 실패 메시지 표시
                        Toast.makeText(this, "로그인 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                // 로그인 실패 시 예외 처리
                Toast.makeText(this, "로그인 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
