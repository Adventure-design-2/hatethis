package com.example.hatethis.ui.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hatethis.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun InviteScreen(
    authViewModel: AuthViewModel,
    userUid: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inviteCode by remember { mutableStateOf("") }
    var partnerCode by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Firestore에서 초대 코드 로드
    LaunchedEffect(userUid) {
        coroutineScope.launch {
            authViewModel.loadUserProfile { profile ->
                inviteCode = profile?.inviteCode.orEmpty()
            }
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
            // 초대 코드 생성 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val generatedCode = authViewModel.generateUniqueInviteCode()
                            val success = authViewModel.updateInviteCode(userUid, generatedCode)
                            isLoading = false
                            if (success) {
                                inviteCode = generatedCode
                                Toast.makeText(context, "초대 코드가 생성되었습니다!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "초대 코드 생성 실패!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "코드 생성 중..." else "초대 코드 생성")
            }
        } else {
            // 초대 코드 표시 및 복사 버튼
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

        // 파트너 초대 코드 입력 필드
        Text(text = "파트너 초대 코드를 입력하세요:")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = partnerCode,
            onValueChange = { partnerCode = it },
            label = { Text("파트너 초대 코드") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 파트너 연결 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    val success = authViewModel.connectPartner(userUid, partnerCode)
                    isLoading = false
                    statusMessage = if (success) {
                        "파트너 연결 성공!"
                    } else {
                        "파트너 연결 실패. 초대 코드를 확인하세요."
                    }
                }
            },
            enabled = partnerCode.isNotEmpty() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "연결 중..." else "파트너 연결")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 상태 메시지 표시
        if (statusMessage.isNotEmpty()) {
            Text(text = statusMessage)
        }
    }
}



@Preview
@Composable
fun PreviwInviteScreen(){
    InviteScreen(authViewModel = AuthViewModel(), userUid = "userUid")
}
