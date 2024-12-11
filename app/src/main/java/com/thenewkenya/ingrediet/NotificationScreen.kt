package com.thenewkenya.ingrediet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.thenewkenya.ingrediet.ui.viewmodel.SupabaseViewModel
import io.github.jan.supabase.postgrest.from
import com.thenewkenya.ingrediet.data.model.Country


@Composable
fun NotificationsScreen() {
    val viewModel: SupabaseViewModel = hiltViewModel()
    val client = viewModel.client
    var countries by remember { mutableStateOf<List<Country>>(listOf()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            countries = client.from("countries").select().decodeList<Country>()
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(countries, key = { country -> country.id }) { country ->
                    Text(country.name)
                }
            }
        }
    }
}