package com.example.hatethis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hatethis.data.DataRepository
import com.example.hatethis.data.FirebaseService
import com.example.hatethis.data.LocalDataStore
import com.example.hatethis.ui.invite.InviteScreen
import com.example.hatethis.ui.login.LoginScreen
import com.example.hatethis.ui.mission.MissionScreen
import com.example.hatethis.ui.profile.ProfileScreen
import com.example.hatethis.ui.records.RecordListScreen
import com.example.hatethis.ui.records.RecordScreen
import com.example.hatethis.ui.register.RegisterScreen
import com.example.hatethis.viewmodel.*
import com.example.hatethis.viewmodel.factory.MissionRecommendationViewModelFactory
import com.example.hatethis.viewmodel.factory.RecordListViewModelFactory
import com.example.hatethis.viewmodel.factory.RecordViewModelFactory
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

            // DataRepository 및 LocalDataStore 초기화
            val firebaseService = FirebaseService()
            val localDataStore = LocalDataStore(context = applicationContext)
            val dataRepository = DataRepository(firebaseService, localDataStore)

            // MissionRecommendationViewModel 초기화
            val missionViewModelFactory = MissionRecommendationViewModelFactory(dataRepository)
            val missionViewModel: MissionRecommendationViewModel =
                ViewModelProvider(this, missionViewModelFactory)[MissionRecommendationViewModel::class.java]

            // Record 관련 ViewModel 초기화
            val recordViewModelFactory = RecordViewModelFactory(dataRepository, authViewModel)
            val recordViewModel: RecordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]

            val recordListViewModelFactory = RecordListViewModelFactory(dataRepository, authViewModel)
            val recordListViewModel: RecordListViewModel = ViewModelProvider(this, recordListViewModelFactory)[RecordListViewModel::class.java]

            // 로그인 상태 관찰
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

            // 네비게이션 구성
            AppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                missionViewModel = missionViewModel,
                recordViewModel = recordViewModel,
                recordListViewModel = recordListViewModel,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionRecommendationViewModel,
    recordViewModel: RecordViewModel,
    recordListViewModel: RecordListViewModel,
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
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToInvite = { navController.navigate("invite") }
            )
        }

        // 회원가입 화면
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // 초대 화면
        composable("invite") {
            InviteScreen(
                authViewModel = authViewModel,
                userUid = authViewModel.getCurrentUserId()
            )
        }

        // 미션 화면
        composable("mission") {
            MissionScreen(
                viewModel = missionViewModel,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToRecordList = { navController.navigate("recordList") },
                onNavigateToRecordInput = { navController.navigate("recordInput") }
            )
        }

        // 프로필 화면
        composable("profile") {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateToMission = { navController.navigate("mission") },
                onNavigateToInvite = { navController.navigate("invite") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }

        // 기록 리스트 화면
        composable("recordList") {
            RecordListScreen(
                viewModel = recordListViewModel,
                onNavigateToMission = { navController.navigate("mission") },
                onAddNewRecord = { navController.navigate("recordInput") },
                onRecordClick = { recordId ->
                    navController.navigate("recordDetail/$recordId")
                }
            )
        }

        // 기록 입력 화면
        composable("recordInput") {
            RecordScreen(
                viewModel = recordViewModel,
                onSaveComplete = {
                    navController.navigate("recordList") {
                        popUpTo("recordInput") { inclusive = true }
                    }
                }
            )
        }
    }
}
