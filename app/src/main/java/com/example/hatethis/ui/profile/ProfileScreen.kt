package com.example.hatethis.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hatethis.model.UserProfile
import com.example.hatethis.viewmodel.AuthViewModel
import com.google.firebase.storage.FirebaseStorage

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateToMission: () -> Unit,
    onNavigateToInvite: () -> Unit, // 초대 화면 이동 콜백
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val storage = FirebaseStorage.getInstance()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Firestore에서 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile { profile ->
            profile?.let {
                name = it.name
                bio = it.bio
                imageUrl = it.imageUrl
            }
        }
    }

    // 이미지 선택 및 업로드
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            val fileName = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("profile_images/$fileName")

            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrl = downloadUri.toString()
                        Toast.makeText(context, "이미지가 업로드되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "프로필 설정", modifier = Modifier.padding(bottom = 16.dp))

        // 유저 초대 버튼
        Button(onClick = onNavigateToInvite, modifier = Modifier.fillMaxWidth()) {
            Text("유저 초대")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 프로필 이미지 표시
        AsyncImage(
            model = imageUrl,
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
        )

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이미지 선택")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 이름 입력 필드
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 소개 입력 필드
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("소개") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 프로필 저장 버튼
        Button(
            onClick = {
                isLoading = true
                val profile = UserProfile(
                    uid = viewModel.getCurrentUserId(),
                    name = name,
                    bio = bio,
                    imageUrl = imageUrl
                )
                viewModel.saveUserProfile(profile) { success ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, "프로필이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "프로필 저장에 실패했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "저장 중..." else "프로필 저장")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 미션 페이지로 이동 버튼
        Button(
            onClick = onNavigateToMission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("미션 페이지로 이동")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 로그아웃 버튼
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그아웃")
        }
    }
}
