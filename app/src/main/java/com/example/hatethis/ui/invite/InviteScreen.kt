package com.example.hatethis.ui.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hatethis.viewmodel.AuthViewModel
import com.example.hatethis.model.UserProfile // UserProfile 클래스 사용
import kotlinx.coroutines.launch
import com.example.hatethis.utils.InviteUtils // 초대 코드 생성 유틸리티
import com.example.hatethis.viewmodel.InviteViewModel

@Composable
fun InviteScreen(
    viewModel: InviteViewModel = viewModel, // InviteViewModel 주입
    userUid: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inviteCode by remember { mutableStateOf("") }
    var partnerCode by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Firestore에서 초대 코드 로드
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile(userUid) { profile ->
            inviteCode = profile?.inviteCode.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (inviteCode.isEmpty()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val generatedCode = com.example.hatethis.utils.InviteUtils.generateInviteCode()
                        viewModel.loadUserProfile(userUid) { profile ->
                            profile?.let {
                                val updatedProfile = it.copy(inviteCode = generatedCode)
                                viewModel.saveUserProfile(updatedProfile) { success ->
                                    isLoading = false
                                    if (success) {
                                        inviteCode = generatedCode
                                        Toast.makeText(context, "초대 코드가 생성되었습니다!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "초대 코드 생성 실패!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "코드 생성 중..." else "초대 코드 생성")
            }
        } else {
            Text(text = "내 초대 코드: $inviteCode")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Invite Code", inviteCode)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, "초대 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("초대 코드 복사")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "파트너 초대 코드를 입력하세요:")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = partnerCode,
            onValueChange = { partnerCode = it },
            label = { Text("파트너 초대 코드") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    viewModel.loadUserProfile(userUid) { profile ->
                        val isConnected = profile?.inviteCode == partnerCode
                        statusMessage = if (isConnected) {
                            "파트너 연결 성공!"
                        } else {
                            "파트너 연결 실패. 초대 코드를 확인하세요."
                        }
                        isLoading = false
                    }
                }
            },
            enabled = partnerCode.isNotEmpty() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "연결 중..." else "파트너 연결")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (statusMessage.isNotEmpty()) {
            Text(text = statusMessage)
        }
    }
}
