package com.example.hatethis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hatethis.ui.login.LoginScreen
import com.example.hatethis.ui.mission.MissionScreen
import com.example.hatethis.ui.register.RegisterScreen
import com.example.hatethis.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = viewModel()

            // 상태 관리를 위해 remember 사용
            var currentScreen by remember { mutableStateOf("login") }

            when (currentScreen) {
                "login" -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { currentScreen = "mission" },
                    onNavigateToRegister = { currentScreen = "register" }
                )
                "register" -> RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { currentScreen = "login" }
                )
                "mission" -> MissionScreen(
                    onLogout = { currentScreen = "login" }
                )
            }
        }
    }
}
