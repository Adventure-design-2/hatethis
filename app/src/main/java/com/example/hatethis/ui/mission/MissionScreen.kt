package com.example.hatethis.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MissionScreen(onLogout: () -> Unit) {
    var mission by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Today's Mission", modifier = Modifier.padding(bottom = 16.dp))

        // Display the mission
        BasicTextField(
            value = mission,
            onValueChange = { mission = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(onClick = { onLogout() }) {
            Text("Logout")
        }
    }
}
