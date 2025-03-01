package com.thenewkenya.ingrediet.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.network.AuthManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val authManager = remember {
        AuthManager(context)
    }
    val coroutineScope = rememberCoroutineScope()

    // Inside your composable
    val user = supabase.auth.currentUserOrNull()
    if (user == null) {
        LoadingScreen()
        coroutineScope.launch {
            authManager.signOut()
            //navController.navigate("login") {
            //    popUpTo("home") { inclusive = true }
            //}
        }
    } else {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome ${user.email}")
            Button(
                onClick = {
                    coroutineScope.launch {
                        authManager.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Sign Out")
            }
        }
    }
}