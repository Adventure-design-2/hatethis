package com.example.hatethis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hatethis.ui.invite.InviteScreen
import com.example.hatethis.ui.login.LoginScreen
import com.example.hatethis.ui.mission.MissionScreen
import com.example.hatethis.ui.profile.ProfileScreen
import com.example.hatethis.ui.records.RecordInputScreen
import com.example.hatethis.ui.records.RecordListScreen
import com.example.hatethis.ui.register.RegisterScreen
import com.example.hatethis.viewmodel.AuthViewModel
import com.example.hatethis.viewmodel.MissionViewModel
import com.example.hatethis.viewmodel.RecordViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel() // AuthViewModel
            val missionViewModel: MissionViewModel = viewModel() // MissionViewModel
            val recordViewModel: RecordViewModel = viewModel() // RecordViewModel

            // 로그인 상태를 collectAsState로 관찰
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

            // 네비게이션 구성
            AppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                missionViewModel = missionViewModel,
                recordViewModel = recordViewModel,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel,
    recordViewModel: RecordViewModel,
    isLoggedIn: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "mission" else "login"
    ) {
        // 로그인 화면
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("mission") {
                        popUpTo("login") { inclusive = true } // 로그인 후 이전 스택 제거
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register") // 회원가입 화면으로 이동
                },
                onNavigateToInvite = {
                    navController.navigate("invite") // 초대 화면으로 이동
                }
            )
        }

        // 회원가입 화면
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true } // 회원가입 후 로그인 화면으로 이동
                    }
                }
            )
        }

        // 초대 화면
        composable("invite") {
            InviteScreen(
                authViewModel = authViewModel, // 전달된 InviteViewModel
                userUid = authViewModel.getCurrentUserId() // userUid 전달
            )
        }

        // 미션 화면
        composable("mission") {
            MissionScreen(
                viewModel = missionViewModel, // 전달된 MissionViewModel
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToRecords = { navController.navigate("records") }, // 기록 목록 화면으로 이동
                onNavigateToRecordInput = { navController.navigate("recordInput") } // 기록 입력 화면으로 이동
            )
        }

        // 프로필 화면
        composable("profile") {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateToMission = { navController.navigate("mission") },
                onNavigateToInvite = { navController.navigate("invite") }, // 초대 화면 이동 추가
                onLogout = {
                    authViewModel.logout() // 로그아웃 처리
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true } // 로그아웃 후 이전 스택 제거
                    }
                }
            )
        }

        // 기록 목록 화면
        composable("records") {
            val userUid = authViewModel.getCurrentUserId() // 사용자 UID 가져오기
            RecordListScreen(
                viewModel = recordViewModel,
                userUid = userUid // 사용자 UID 전달
            )
        }


        // 기록 입력 화면
        composable("recordInput") {
            val userUid = authViewModel.getCurrentUserId() // 현재 사용자 UID 가져오기
            RecordInputScreen(
                viewModel = recordViewModel,
                userUid = userUid, // 사용자 UID 전달
                onNavigateToRecordList = { navController.navigate("records") } // 저장 후 기록 목록 화면으로 이동
            )
        }
    }
}
