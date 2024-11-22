package com.thenewkenya.ingrediet.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject


@HiltViewModel
class SupabaseViewModel @Inject constructor(
    val client: SupabaseClient
) : ViewModel() {

}