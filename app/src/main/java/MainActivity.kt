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
import com.example.hatethis.ui.register.RegisterScreen
import com.example.hatethis.viewmodel.AuthViewModel
import com.example.hatethis.viewmodel.MissionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel() // AuthViewModel
            val missionViewModel: MissionViewModel = viewModel() // MissionViewModel

            // 로그인 상태를 collectAsState로 관찰
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

            // 네비게이션 구성
            AppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                missionViewModel = missionViewModel,
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
                onNavigateToProfile = { navController.navigate("profile") }
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
    }
}
