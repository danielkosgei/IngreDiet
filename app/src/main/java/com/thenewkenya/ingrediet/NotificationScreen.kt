package com.thenewkenya.ingrediet

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.thenewkenya.ingrediet.ui.viewmodel.SupabaseViewModel
import io.github.jan.supabase.postgrest.from
import com.thenewkenya.ingrediet.data.model.Country


@Composable
fun NotificationsScreen() {
    val viewModel: SupabaseViewModel = hiltViewModel()
    val client = viewModel.client // Access the SupabaseClient from the ViewModel
    var countries by remember { mutableStateOf<List<Country>>(listOf()) }

    LaunchedEffect(Unit) {
        try {
            countries = client.from("countries").select().decodeList<Country>()
        } catch (e: Exception) {
            // Handle error
        }
    }

    LazyColumn {
        items(countries, key = { country -> country.id }) { country ->
            Text(country.name)
        }
    }
}